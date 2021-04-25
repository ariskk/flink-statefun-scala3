import scala.jdk.FutureConverters._

import org.apache.flink.statefun.sdk.java.StatefulFunctionSpec
import org.apache.flink.statefun.sdk.java.StatefulFunctions
import org.apache.flink.statefun.sdk.java.handler.RequestReplyHandler
import org.apache.flink.statefun.sdk.java.slice.Slices
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.undertow.UndertowOptions.ENABLE_HTTP2
import zio._

@main def run: Unit = buildServer.start

def buildServer = {
  given namespace: NameSpace = NameSpace("com.ariskk")

  val fn = TrackingReducerFn.apply
  val spec = StatefulFunctionSpec
    .builder(fn.typeName)
    .withValueSpec(fn.valueSpec)
    .withSupplier(() => fn)
    .build()

  val functions = new StatefulFunctions
  functions.withStatefulFunction(spec)

  val handler = functions.requestReplyHandler()

  val server = Undertow
    .builder()
    .addHttpListener(5000, "0.0.0.0")
    .setHandler(httpHandler(handler))
    .setServerOption(ENABLE_HTTP2, true)
    .build()

  server
}

def httpHandler(replyHandler: RequestReplyHandler) = new HttpHandler {
  private final val handler = replyHandler

  override def handleRequest(exchange: HttpServerExchange) =
    exchange.getRequestReceiver().receiveFullBytes(onRequestBody)

  def onRequestBody(exchange: HttpServerExchange, requestBytes: Array[Byte]): Unit = {
    exchange.dispatch() // Weird Java Voodoo?

    lazy val program = for {
      response <- ZIO.fromFuture(_ => handler.handle(Slices.wrap(requestBytes)).asScala)
      _ <- ZIO.effect {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/octet-stream")
        exchange.getResponseSender().send(response.asReadOnlyByteBuffer())
      }
    } yield ()

    Runtime
      .default
      .unsafeRun(
        program.catchAll { case e: Exception =>
          exchange.getResponseHeaders().put(Headers.STATUS, 500)
          ZIO(exchange.endExchange())
        }
      )

  }

}

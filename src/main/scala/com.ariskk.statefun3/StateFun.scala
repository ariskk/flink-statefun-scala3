package com.ariskk.statefun3

import java.util.concurrent.CompletableFuture
import scala.concurrent.Future
import scala.jdk.OptionConverters._

import org.apache.flink.statefun.sdk.java.Context
import org.apache.flink.statefun.sdk.java.StatefulFunction
import org.apache.flink.statefun.sdk.java.TypeName
import org.apache.flink.statefun.sdk.java.ValueSpec
import org.apache.flink.statefun.sdk.java.message.Message
import org.apache.flink.statefun.sdk.java.types.Type
import org.apache.flink.statefun.sdk.java.AddressScopedStorage
import org.apache.flink.statefun.sdk.java.io.KafkaEgressMessage
import io.circe.Codec

abstract class StateFun[
  In: Typer,
  Out: Namer: Record: Codec,
  State: Namer: Typer
](using ns: NameSpace)
  extends StatefulFunction:

  def functionName: String

  def fnTypeName: TypeName =
    TypeName.typeNameFromString(s"${ns.value}.fn/$functionName")

  def stateType = Typer[State].giveType

  def valueSpec = ValueSpec.named(Namer[State].name).withCustomType(Typer[State].giveType)

  def fetchState: AddressScopedStorage => Option[State] = { storage =>
    storage.get(valueSpec).toScala
  }

  def updateState: (AddressScopedStorage, State) => Unit = { (storage, state) =>
    storage.set(valueSpec, state)
  }

  def decodeMessage: Message => In = { msg =>
    msg.as(Typer[In].giveType)
  }

  def process: (In, Option[State]) => (Out, State)

  def sendOutput(context: Context, out: Out) = context.send(
    KafkaEgressMessage
      .forEgress(TypeName.typeNameFromString(Namer[Out].egressName))
      .withTopic(Record[Out].topic)
      .withUtf8Key(Record[Out].key(out))
      .withUtf8Value(Record[Out].payload(out))
      .build()
  )

  override def apply(context: Context, message: Message): CompletableFuture[Void] = {
    val storage             = context.storage()
    val in                  = decodeMessage(message)
    val (out, updatedState) = process(in, fetchState(storage))
    updateState(storage, updatedState)
    sendOutput(context, out)
    context.done()
  }

end StateFun

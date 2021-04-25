import scala.quoted
import scala.quoted._

import org.apache.flink.statefun.sdk.java.types.Type
import org.apache.flink.statefun.sdk.java.types.SimpleType
import org.apache.flink.statefun.sdk.java.TypeName
import io.circe._
import io.circe.parser._

trait Namer[T]:
  def name: String
  def typesName(using ns: NameSpace)   = s"${ns.value}.types/$name"
  def ingressName(using ns: NameSpace) = s"${ns.value}.ingress/$name"
  def egressName(using ns: NameSpace)  = s"${ns.value}.egress/$name"

object Namer:
  def apply[T](using t: Namer[T]) = t

trait Typer[T]:
  def giveType: Type[T]

object Typer:
  def apply[T](using t: Typer[T]) = t

given circeTyper[T](using codec: Codec[T], namer: Namer[T], ns: NameSpace): Typer[T] with
  def giveType: Type[T] = SimpleType.simpleImmutableTypeFrom(
    TypeName.typeNameFromString(namer.typesName),
    t => codec.apply(t).noSpaces.getBytes,
    bytes =>
      parse(new String(bytes))
        .flatMap(json => codec.apply(json.hcursor))
        .getOrElse(
          throw new Exception("Production software will handle this better")
        )
  )

trait Record[T]:
  def topic: String
  def key(t: T): String
  def timestamp(t: T): Long
  def payload(t: T)(using codec: Codec[T]): String = codec.apply(t).noSpaces

object Record:
  def apply[T](using t: Record[T]) = t

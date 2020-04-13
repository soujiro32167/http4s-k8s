import io.circe.{Decoder, Encoder, Json}

package object shims {
  trait IntOrString
  case class IntValue(value: Int)       extends IntOrString
  case class StringValue(value: String) extends IntOrString

  object IntOrString {
    implicit val encode: Encoder[IntOrString] = {
      case IntValue(int)       => Json.fromInt(int)
      case StringValue(string) => Json.fromString(string)
    }

//    implicit val decode: Decoder[IntOrString] = cursor => {
//      val decodeInt    = cursor.as[Int].map(IntValue)
//      val decodeString = cursor.as[String].map(StringValue)
//      decodeInt.leftFlatMap(_ => decodeString)
//    }
    implicit val dec: Decoder[IntOrString] = Decoder.decodeInt.map(IntValue) or Decoder.decodeString.map(StringValue)
  }

  type `intstr.IntOrString` = IntOrString
  type `int-or-string` = IntOrString
  type byte = Byte
  type `resource.Quantity` = IntOrString
}


package secure.logging.auto

import shapeless._
import secure.logging._
import shapeless.labelled.FieldType

/** Shapeless based derivation of [[LogSecureEncoder]] for types with generic representation.
  */
trait LogSecureEncoderAuto {
    implicit val deriveHNil: LogSecureEncoder[HNil] = LogSecureEncoder.instance(_ => LogSecured(""))

    implicit def hlistEncoder[K <: Symbol, H, T <: HList](implicit
        fieldWitness: Witness.Aux[K],
        headParser: Lazy[LogSecureEncoder[FieldType[K, H]]],
        tailParser: LogSecureEncoder[T]
    ): LogSecureEncoder[FieldType[K, H] :: T] = {
        LogSecureEncoder.instance { value =>
            val fieldName: String = fieldWitness.value.name
            val head              = headParser.value.encode(value.head)
            val headNamed         = s"$fieldName: ${head.value}"

            val tail = tailParser.encode(value.tail)
            if (tail.value.isEmpty) {
                LogSecured(headNamed)
            } else {
                LogSecured(s"$headNamed, ${tail.value}")
            }
        }
    }

    implicit def genericEncoder[A, R <: HList](implicit
        generic: LabelledGeneric.Aux[A, R],
        encoder: Lazy[LogSecureEncoder[R]]
    ): LogSecureEncoder[A] = {
        LogSecureEncoder.instance { value =>
            val r       = generic.to(value)
            val encoded = encoder.value.encode(r)
            LogSecured(s"(${encoded.value})")
        }
    }
}

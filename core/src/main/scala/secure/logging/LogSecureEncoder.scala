package secure.logging

import java.nio.charset.Charset
import java.security.MessageDigest
import scala.annotation.implicitNotFound
import scala.collection.immutable.SortedMap

/** Type class that provides a way to encode value of type `A` to [[LogSecured]].
  */
@implicitNotFound("Cannot resolve log secure encoder of the type `${A}`")
trait LogSecureEncoder[-A] {

    /** Encodes value of type `A` to [[LogSecured]].
      *
      * @param value
      *   to be encoded
      * @return
      *   [[LogSecured]] value
      */
    def encode[B <: A](value: B): LogSecured

    /** Helper function that allows to create encoder for type `B` based on encoder for type `A`.
      *
      * @param f
      *   function that converts `B` to `A`
      * @tparam B
      *   type of value
      * @return
      *   encoder for type `B`
      */
    def contraMap[B](f: B => A): LogSecureEncoder[B] = LogSecureEncoder.instance { value: B =>
        encode(f(value))
    }

    /** Helper function that allows to create new encoder for type `A` which will prefix encoded
      * value with `prefix`.
      *
      * @param prefix
      *   prefix to be added to encoded value
      * @return
      *   encoder for type `A`
      */
    def prefix(prefix: String): LogSecureEncoder[A] = LogSecureEncoder.instance { value: A =>
        LogSecured(prefix + encode(value).value)
    }

    /** Helper function that allows to create encoder for type `A` combining it with encoder for
      * type `B`. Resulting encoder will concatenate encoded strings.
      *
      * @param other
      *   encoder for type `A`
      * @tparam B
      *   type of value
      * @return
      *   combined encoder for type `A`
      */
    def |+|[B <: A](other: LogSecureEncoder[B]): LogSecureEncoder[B] = LogSecureEncoder.instance {
        value: B =>
            val secured: LogSecured = other.encode(value)
            this.encode(value) |+| secured
    }
}

trait LogSecureEncoderLowPriority {
    def apply[A](implicit encoder: LogSecureEncoder[A]): LogSecureEncoder[A] = encoder

    def instance[A](f: A => LogSecured): LogSecureEncoder[A] = new LogSecureEncoder[A] {
        override def encode[B <: A](value: B): LogSecured = f(value)
    }

    implicit def encode[A](value: A)(implicit encoder: LogSecureEncoder[A]): LogSecured =
        encoder.encode(value)
}

private[logging] trait LogSecureEncoderLowPriorityImplicits {
    this: LogSecureEncoderLowPriority =>

    implicit val byteEncoder: LogSecureEncoder[Byte] = instance[Byte](i => LogSecured(i.toString))

    implicit val intEncoder: LogSecureEncoder[Int] = instance[Int](i => LogSecured(i.toString))

    implicit val longEncoder: LogSecureEncoder[Long] = instance[Long](i => LogSecured(i.toString))

    implicit val floatEncoder: LogSecureEncoder[Float] =
        instance[Float](i => LogSecured(i.toString))

    implicit val doubleEncoder: LogSecureEncoder[Double] =
        instance[Double](i => LogSecured(i.toString))

    implicit val booleanEncoder: LogSecureEncoder[Boolean] =
        instance[Boolean](i => LogSecured(i.toString))

    implicit def seqEncoder[A: LogSecureEncoder]: LogSecureEncoder[Seq[A]] = {
        iteratorEncoder[A].contraMap(_.iterator)
    }

    implicit def arrayEncoder[A: LogSecureEncoder]: LogSecureEncoder[Array[A]] = {
        iteratorEncoder[A].contraMap(_.iterator)
    }

    implicit def setEncoder[A: LogSecureEncoder]: LogSecureEncoder[Set[A]] = {
        iteratorEncoder[A].contraMap(_.iterator)
    }

    def iteratorEncoder[A](implicit encoder: LogSecureEncoder[A]): LogSecureEncoder[Iterator[A]] = {
        instance[Iterator[A]] { value =>
            LogSecured(value.map(encoder.encode).map(_.value).mkString(s"(", ", ", ")"))
        }
    }

    implicit def optionEncoder[A: LogSecureEncoder]: LogSecureEncoder[Option[A]] = {
        instance[Option[A]] {
            case Some(value) => LogSecured(s"Some(${LogSecureEncoder[A].encode(value).value})")
            case None        => LogSecured("None")
        }
    }

    implicit def eitherEncoder[A: LogSecureEncoder, B: LogSecureEncoder]
        : LogSecureEncoder[Either[A, B]] = {
        instance[Either[A, B]] {
            case Left(value)  => LogSecured(s"Left(${LogSecureEncoder[A].encode(value).value})")
            case Right(value) => LogSecured(s"Right(${LogSecureEncoder[B].encode(value).value})")
        }
    }

    implicit def mapEncoder[K: LogSecureEncoder, V: LogSecureEncoder]
        : LogSecureEncoder[Map[K, V]] = {
        instance[Map[K, V]] { value =>
            val string = value
                .map { case (key, value) =>
                    s"${LogSecureEncoder[K].encode(key).value} -> ${LogSecureEncoder[V].encode(value).value}"
                }
                .mkString("(", ", ", ")")
            LogSecured(string)
        }
    }
}

trait LogSecureEncoderInstances {
    this: LogSecureEncoderLowPriority =>

    val DefaultCharset: Charset = Charset.forName("UTF-8")

    /** SHA-256 encoder for strings. Uses UTF-8 charset.
      */
    val sha256: LogSecureEncoder[String] = sha256(DefaultCharset)

    /** SHA-256 encoder for strings with custom charset.
      *
      * For instance:
      * {{{
      * import secure.logging.core._
      * import secure.logging.core.LogSecureEncoder._
      *
      * case class User(email: String)
      *
      * object User {
      *    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder
      *    .sha256(Charset.forName("UTF-8"))
      *    .contraMap[User](_.email)
      * }
      *
      * val user = new User("john.doe@acme.com")
      * val logLine = sl"user: $user"
      * }}}
      *
      * Will produce string:
      * {{{
      * user: 36d6de708b54f80f4e673d0a09bc1e21c8fb52b267b9afbe812f8000b1ab9590
      * }}}
      *
      * @param charset
      *   charset to be used for encoding
      * @return
      *   encoder for strings that produces SHA-256 hash from the input string
      */
    def sha256(charset: Charset): LogSecureEncoder[String] = hash(charset, "SHA-256")

    /** SHA-512 encoder for strings. Uses UTF-8 charset.
      */
    val sha512: LogSecureEncoder[String] = sha512(DefaultCharset)

    /** SHA-512 encoder for strings with custom charset. For instance:
      * {{{
      * import secure.logging.core._
      * import secure.logging.core.LogSecureEncoder._
      *
      * case class User(email: String)
      *
      * object User {
      *    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder
      *    .sha512(Charset.forName("UTF-8"))
      *    .contraMap[User](_.email)
      * }
      *
      * val user = new User("john.doe@acme.com")
      * val logLine = sl"user: $user"
      * }}}
      *
      * Will produce string:
      * {{{
      * user: 9282184f164e8aac5f62f57eaf04aee142ed205e9befceed052b757cfe1ba60da288c3fa38f734d7035e57513ade3a9d4b961e7aa790260d0ee3470c8cf1e146
      * }}}
      *
      * @param charset
      *   charset to be used for encoding
      * @return
      *   encoder for strings that produces SHA-512 hash from the input string
      */
    def sha512(charset: Charset): LogSecureEncoder[String] = hash(charset, "SHA-512")

    /** Generic hash encoder for strings with custom charset and hashing algorithm. See
      * documentation for [[MessageDigest]] for list of supported algorithms.
      *
      * @param charset
      *   charset to be used for encoding string into bytes for hashing
      * @param algorithm
      *   hashing algorithm
      * @return
      *   encoder for strings that produces hash from the input string
      */
    def hash(charset: Charset, algorithm: String): LogSecureEncoder[String] = instance {
        value: String =>
            val digest = MessageDigest.getInstance(algorithm)
            digest.update(value.getBytes(charset))
            LogSecured(digest.digest().map("%02x".format(_)).mkString)
    }

    /** Masks string with asterisk character with prefix length of 10.
      */
    val mask: LogSecureEncoder[String] = maskPrefix(10)

    /** Masks string prefix with custom replacement character and prefix length. In case prefix
      * length is greater then string length, whole string will be masked. Default masking character
      * is asterisk (*).
      *
      * For instance:
      * {{{
      *  import secure.logging.core._
      *  import secure.logging.core.LogSecureEncoder._
      *
      *  case class User(email: String)
      *
      *  object User {
      *    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskPrefix(10, '#').contraMap[User](_.email)
      *  }
      *
      *  val user = new User("john.doe@acme.com")
      *  val logLine = sl"user: $user"
      * }}}
      *
      * Will produce string:
      * {{{
      *  user: ##########cme.com
      * }}}
      *
      * @param replacement
      *   character to be used for masking
      * @param prefixLength
      *   length of prefix to be masked
      * @return
      *   encoder for strings
      */
    def maskPrefix(prefixLength: Int, replacement: Char = '*'): LogSecureEncoder[String] =
        instance { value: String =>
            val maskLength = Math.min(value.length, prefixLength)
            val suffix     = value.drop(prefixLength)
            LogSecured(replacement.toString * maskLength + suffix)
        }

    /** Masks string with custom replacement character and suffix length. In case suffix length is
      * greater then string length, whole string will be masked. Default masking character is
      * asterisk (*).
      *
      * For instance:
      * {{{
      *  import secure.logging.core._
      *  import secure.logging.core.LogSecureEncoder._
      *
      *  case class User(email: String)
      *
      *  object User {
      *    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10, '#').contraMap[User](_.email)
      *  }
      *
      *  val user = new User("john.doe@acme.com")
      *  val logLine = sl"user: $user"
      * }}}
      *
      * Will produce string:
      * {{{
      *  user: john.do##########
      * }}}
      */
    def maskSuffix(suffixLength: Int, replacement: Char = '*'): LogSecureEncoder[String] =
        instance { value: String =>
            val maskLength = Math.min(value.length, suffixLength)
            val prefix     = value.dropRight(suffixLength)
            LogSecured(prefix + replacement.toString * maskLength)
        }
}

trait LogSecureEncoders
    extends LogSecureEncoderLowPriority
    with LogSecureEncoderInstances
    with LogSecureEncoderLowPriorityImplicits

object LogSecureEncoder extends LogSecureEncoders

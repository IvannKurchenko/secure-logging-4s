package secure.logging

import java.nio.charset.Charset
import scala.annotation.implicitNotFound

/**
 * Type class that provides a way to encode value of type `A` to [[LogSecured]].
 */
@implicitNotFound("Cannot resolve log secure encoder of the type ${T}")
trait LogSecureEncoder[-A] {

  /**
   * Encodes value of type `A` to [[LogSecured]].
   *
   * @param value to be encoded
   * @tparam B type of value
   * @return [[LogSecured]] value
   */
  def encode[B <: A](value: B): LogSecured

  /**
   * Helper function that allows to create encoder for type `B` based on encoder for type `A`.
   *
   * @param f function that converts `B` to `A`
   * @tparam B type of value
   * @return encoder for type `B`
   */
  def contraMap[B](f: B => A): LogSecureEncoder[B] = LogSecureEncoder.instance { value: B =>
    encode(f(value))
  }
}

trait LogSecureEncoderLowPriority {
  def apply[A](implicit encoder: LogSecureEncoder[A]): LogSecureEncoder[A] = encoder

  def instance[A](f: A => LogSecured): LogSecureEncoder[A] = new LogSecureEncoder[A] {
    override def encode[B <: A](value: B): LogSecured = f(value.asInstanceOf[A])
  }

  implicit def encode[A](value: A)(implicit encoder: LogSecureEncoder[A]): LogSecured = encoder.encode(value)
}


trait LogSecureEncoderLowPriorityImplicits {
  this: LogSecureEncoderLowPriority =>

  implicit val Byte: LogSecureEncoder[Byte] = instance[Byte](i => LogSecured(i.toString))
  implicit val Integer: LogSecureEncoder[Int] = instance[Int](i => LogSecured(i.toString))
  implicit val Long: LogSecureEncoder[Int] = instance[Int](i => LogSecured(i.toString))
  implicit val Float: LogSecureEncoder[Float] = instance[Float](i => LogSecured(i.toString))
  implicit val Double: LogSecureEncoder[Double] = instance[Double](i => LogSecured(i.toString))
  implicit val Boolean: LogSecureEncoder[Boolean] = instance[Boolean](i => LogSecured(i.toString))


  implicit def optionEncoder[A](implicit encoder: LogSecureEncoder[A]): LogSecureEncoder[Option[A]] = instance[Option[A]] {
    case Some(value) => encoder.encode(value)
    case None => LogSecured("None")
  }

  implicit def iterableEncoder[A](separator: String = ", ")
                                 (implicit encoder: LogSecureEncoder[A]): LogSecureEncoder[Iterable[A]] = instance[Iterable[A]] { value =>
    LogSecured(value.map(encoder.encode).map(_.value).mkString(separator))
  }

  implicit def eitherEncoder[A, B](implicit encoderA: LogSecureEncoder[A],
                                   encoderB: LogSecureEncoder[B]): LogSecureEncoder[Either[A, B]] = instance[Either[A, B]] {
    case Left(value) => encoderA.encode(value)
    case Right(value) => encoderB.encode(value)
  }
}

trait LogSecureEncoderInstances {
  this: LogSecureEncoderLowPriority =>

  val DefaultCharset: Charset = Charset.forName("UTF-8")

  /**
   * SHA-256 encoder for strings. Uses UTF-8 charset.
   * For instance:
   * {{{
   * import secure.logging.core._
   * import secure.logging.core.LogSecureEncoder._
   *
   * case class User(email: String)
   *
   * object User {
   * implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
   * }
   *
   * val user = new User("john.doe@acme.com")
   * val logLine = sl"user: $user"
   * }}}
   *
   * Will produce string:
   * {{{
   * user:
   * }}}
   */
  val sha256: LogSecureEncoder[String] = sha256(DefaultCharset)

  /**
   * SHA-256 encoder for strings with custom charset.
   *
   * @param charset charset to be used for encoding
   * @return encoder for strings
   */
  def sha256(charset: Charset): LogSecureEncoder[String] = hash(charset, "SHA-256")

  /**
   * SHA-512 encoder for strings. Uses UTF-8 charset.
   */
  def sha512: LogSecureEncoder[String] = sha512(DefaultCharset)

  /**
   * SHA-512 encoder for strings with custom charset.
   *
   * @param charset charset to be used for encoding
   * @return encoder for strings
   */
  def sha512(charset: Charset): LogSecureEncoder[String] = hash(charset, "SHA-512")

  def hash(charset: Charset, algorithm: String): LogSecureEncoder[String] = instance { value: String =>
    val digest = java.security.MessageDigest.getInstance(algorithm)
    digest.update(value.getBytes(charset))
    LogSecured(digest.digest().map("%02x".format(_)).mkString)
  }

  /**
   * Masks string with asterisk character with prefix length of 10.
   */
  val mask: LogSecureEncoder[String] = maskPrefix(10, '*')


  /**
   * Masks string with custom replacement character.
   * For instance:
   * {{{
   *  import secure.logging.core._
   *  import secure.logging.core.LogSecureEncoder._
   *
   *  case class User(email: String)
   *
   *  object User {
   *    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.mask.contraMap[User](_.email)
   *  }
   *
   *  val user = new User("john.doe@acme.com")
   *  val logLine = sl"user: $user"
   * }}}
   *
   * Will produce string:
   * {{{
   *  user: *********cme.com
   * }}}
   *
   * @param replacement  character to be used for masking
   * @param prefixLength length of prefix to be masked
   * @return encoder for strings
   * */
  def maskPrefix(prefixLength: Int, replacement: Char = '*'): LogSecureEncoder[String] = instance { value: String =>
    LogSecured(replacement.toString * prefixLength + value.drop(prefixLength))
  }

  /**
   * Masks string with custom replacement character.
   * For instance:
   * {{{
   *  import secure.logging.core._
   *  import secure.logging.core.LogSecureEncoder._
   *
   *  case class User(email: String)
   *
   *  object User {
   *    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
   *  }
   *
   *  val user = new User("john.doe@acme.com")
   *  val logLine = sl"user: $user"
   * }}}
   *
   *
   * Will produce string:
   * {{{
   *  user: john.do*********
   * }}}
   *
   * */
  def maskSuffix(suffixLength: Int, replacement: Char = '*'): LogSecureEncoder[String] = instance { value: String =>
    LogSecured(value.dropRight(suffixLength) + replacement.toString * suffixLength)
  }
}

trait LogSecureEncoders
  extends LogSecureEncoderLowPriority
    with LogSecureEncoderInstances
    with LogSecureEncoderLowPriorityImplicits

object LogSecureEncoder extends LogSecureEncoders


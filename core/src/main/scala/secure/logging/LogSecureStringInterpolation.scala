package secure.logging

trait LogSecureStringInterpolation {
  /**
   * Scala string interpolator that converts string to [[LogSecureString]] based on implicit [[LogSecureEncoder]].
   * For instance:
   * {{{
   *  import secure.logging.core._
   *  import secure.logging.core.LogSecureEncoder._
   *
   *  case class User(email: String)
   *
   *  object User {
   *    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.sha256.contraMap[User](_.email)
   *  }
   *
   *  val user = new User("john.doe@acme.com")
   *  val logLine = sl"user: $user"
   * }}}
   *
   * Will produce string:
   * {{{
   * user: 36d6de708b54f80f4e673d0a09bc1e21c8fb52b267b9afbe812f8000b1ab9590
   * }}}
   */
  implicit final class LogSecureStringInterpolator(val sc: StringContext) {
    def sl(args: LogSecured*): LogSecureString = {
      new LogSecureString(sc.s(args.map(_.value): _*))
    }
  }
}

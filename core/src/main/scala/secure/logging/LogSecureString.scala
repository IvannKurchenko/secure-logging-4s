package secure.logging

/** Log secure string is a string that does not contain any sensitive information in plain text.
  * Hence it is safe to log it. Constructor is private, so it can be created only from within the
  * library. Can be created only from [[LogSecureString]] interpolation, which on it's turn expects
  * [[LogSecureEncoder]] to be in scope, so it can encode string in a secure way.
  *
  * For instance, to create secure string with sha-256 hash of a password:
  * {{{
  *
  *  import secure.logging._
  *
  *  implicit val encoder = LogSecureEncoder.sha256
  *  val email = "john.doe@acme.com"
  *  val message = sl"User logged in: $email"
  * }}}
  * `message` will be of type `LogSecureString` and will contain sha-256 hash of the email.
  *
  * @param value
  *   string value
  */
final case class LogSecureString(value: String) extends AnyVal {
    /*
     * Prevents string to be copied and modified
     */
    private def copy(): Unit = ()
}

object LogSecureString {

    /** Private modifier prevents from creating secure string outside of library internals and
      * guarantees string won't be created in a wrong way. For instance, it's content won't have
      * unwanted piece of information in a plain form.
      */
    private[logging] def apply(value: String): LogSecureString = new LogSecureString(value)
}

package secure.logging

/**
 * Log secure string is a string that does not contain any sensitive information in plain text.
 * Hence it is safe to log it.
 *
 * @param value string value
 */
final case class LogSecureString(value: String) extends AnyVal {
  /*
   * Prevents string to be copied and modified
   */
  private def copy(): Unit = ()
}


object LogSecureString {
  /**
   * Private modifier prevents from creating secure string outside of library internals
   * and guarantees string won't be created in a wrong way. For instance, it's content
   * won't have unwanted piece of information in a plain form.
   */
  private[logging] def apply(value: String): LogSecureString = new LogSecureString(value)
}

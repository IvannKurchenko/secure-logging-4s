package secure.logging


/**
 * Piece of log string that should be logged in å secure way. Takes part in [[LogSecureString]] interpolation.
 * While constructor is public, be careful what you pass to it!
 *
 * @param value string to be logged
 */
final case class LogSecured(value: String) extends AnyVal {
  /*
   * Prevents string to be copied and modified
   */
  private def copy(): Unit = ()
}

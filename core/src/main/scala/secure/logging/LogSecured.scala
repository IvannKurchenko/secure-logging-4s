package secure.logging


/**
 * Piece of log string that should be logged in å secure way. Takes part in [[LogSecureString]] interpolation.
 */
final case class LogSecured(value: String) extends AnyVal {
  /*
   * Prevents string to be copied and modified
   */
  private def copy(): Unit = ()
}

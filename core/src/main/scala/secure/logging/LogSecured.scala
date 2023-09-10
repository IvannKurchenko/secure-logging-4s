package secure.logging

/** Piece of log string that should be logged in a secure way. Takes part in [[LogSecureString]]
  * interpolation. While constructor is public, be careful what you pass to it!
  *
  * @param value
  *   string to be logged
  */
final case class LogSecured(value: String) extends AnyVal {

    /** Concatenates current [[LogSecured]] with another one.
      */
    def |+|(other: LogSecured): LogSecured = LogSecured(value + other.value)

    /*
     * Prevents string to be copied and modified
     */
    private def copy(): Unit = ()
}

package secure

package object logging extends LogSecureStringInterpolation with LogSecureEncoders {
    implicit class StringOps(private val value: String) extends AnyVal {

        /** Converts string to [[LogSecured]] as is.
          */
        def plain: LogSecured = LogSecured(value)
    }
}

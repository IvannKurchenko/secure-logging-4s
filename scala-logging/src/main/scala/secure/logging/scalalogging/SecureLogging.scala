package secure.logging.scalalogging

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/** Similar to [[com.typesafe.scalalogging.LazyLogging]], but for SecureScalaLogger.
  */
trait LazySecureLogging {
    @transient
    protected lazy val logger: SecureScalaLogger =
        SecureScalaLogger(Logger(LoggerFactory.getLogger(getClass.getName)))
}

/** Similar to StrictLogging, but for SecureScalaLogger.
  */
trait StrictSecureLogging {

    protected val logger: SecureScalaLogger =
        SecureScalaLogger(Logger(LoggerFactory.getLogger(getClass.getName)))
}

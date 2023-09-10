package secure.logging.log4s

import org.log4s.Logger.LevelLogger
import org.log4s._
import secure.logging._

class SecureLevelLogger(val underlying: LevelLogger) extends AnyVal {
    def isEnabled: Boolean = underlying.isEnabled

    def apply(msg: => String): Unit = underlying(msg)

    def apply(t: Throwable)(msg: => String): Unit = underlying(t)(msg)
}

/** Wrapper around [[org.log4s.Logger]] that allows to log secure strings ONLY.
  * @param logger
  *   underlying logger
  */
class SecureLogger(logger: Logger) {
    @inline def name: String = logger.name

    @inline def isTraceEnabled: Boolean = logger.isTraceEnabled

    @inline def isDebugEnabled: Boolean = logger.isDebugEnabled

    @inline def isInfoEnabled: Boolean = logger.isInfoEnabled

    @inline def isWarnEnabled: Boolean = logger.isWarnEnabled

    @inline def isErrorEnabled: Boolean = logger.isErrorEnabled

    def apply(lvl: LogLevel): SecureLevelLogger = new SecureLevelLogger(logger(lvl))

    def trace(t: Throwable)(msg: => LogSecureString): Unit = logger.trace(t)(msg.value)

    def trace(msg: => LogSecureString): Unit = logger.trace(msg.value)

    def debug(t: Throwable)(msg: => LogSecureString): Unit = logger.debug(t)(msg.value)

    def debug(msg: => LogSecureString): Unit = logger.debug(msg.value)

    def info(t: Throwable)(msg: => LogSecureString): Unit = logger.info(t)(msg.value)

    def info(msg: => LogSecureString): Unit = logger.info(msg.value)

    def warn(t: Throwable)(msg: => LogSecureString): Unit = logger.warn(t)(msg.value)

    def warn(msg: => LogSecureString): Unit = logger.warn(msg.value)

    def error(t: Throwable)(msg: => LogSecureString): Unit = logger.error(t)(msg.value)

    def error(msg: => LogSecureString): Unit = logger.error(msg.value)
}

object SecureLogger {
    def apply(logger: Logger): SecureLogger = new SecureLogger(logger)

    implicit class SecureLoggerOps(logger: Logger) {
        def secure: SecureLogger = new SecureLogger(logger)
    }
}

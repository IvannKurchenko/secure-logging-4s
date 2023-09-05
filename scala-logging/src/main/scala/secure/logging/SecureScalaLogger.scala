package secure.logging

import com.typesafe.scalalogging.Logger
import org.slf4j.Marker

/**
 * A secure logger that wraps a Scala logger.
 *
 * @param underlying the Scala logger to wrap
 */
class SecureScalaLogger(underlying: Logger) {
  // Error

  def error(message: LogSecureString): Unit = underlying.error(message.value)

  def error(message: LogSecureString, cause: Throwable): Unit = underlying.error(message.value, cause)

  def error(marker: Marker, message: LogSecureString): Unit = underlying.error(marker, message.value)

  def error(marker: Marker, message: LogSecureString, cause: Throwable): Unit = underlying.error(marker, message.value, cause)

  def whenErrorEnabled(body: Unit): Unit = underlying.whenErrorEnabled(body)

  // Warn

  def warn(message: LogSecureString): Unit = underlying.warn(message.value)

  def warn(message: LogSecureString, cause: Throwable): Unit = underlying.warn(message.value, cause)

  def warn(marker: Marker, message: LogSecureString): Unit = underlying.warn(marker, message.value)

  def warn(marker: Marker, message: LogSecureString, cause: Throwable): Unit = underlying.warn(marker, message.value, cause)

  def whenWarnEnabled(body: Unit): Unit = underlying.whenWarnEnabled(body)

  // Info

  def info(message: LogSecureString): Unit = underlying.info(message.value)

  def info(message: LogSecureString, cause: Throwable): Unit = underlying.info(message.value, cause)

  def info(marker: Marker, message: LogSecureString): Unit = underlying.info(marker, message.value)

  def info(marker: Marker, message: LogSecureString, cause: Throwable): Unit = underlying.info(marker, message.value, cause)

  def info(marker: Marker, message: LogSecureString, args: Any*): Unit = underlying.info(marker, message.value, args)

  def whenInfoEnabled(body: Unit): Unit = underlying.whenInfoEnabled(body)

  // Debug

  def debug(message: LogSecureString): Unit = underlying.debug(message.value)

  def debug(message: LogSecureString, cause: Throwable): Unit = underlying.debug(message.value, cause)

  def debug(message: LogSecureString, args: Any*): Unit = underlying.debug(message.value, args)

  def debug(marker: Marker, message: LogSecureString): Unit = underlying.debug(marker, message.value)

  def debug(marker: Marker, message: LogSecureString, cause: Throwable): Unit = underlying.debug(marker, message.value, cause)

  def debug(marker: Marker, message: LogSecureString, args: Any*): Unit = underlying.debug(marker, message.value, args)

  def whenDebugEnabled(body: Unit): Unit = underlying.whenDebugEnabled(body)

  // Trace

  def trace(message: LogSecureString): Unit = underlying.trace(message.value)

  def trace(message: LogSecureString, cause: Throwable): Unit = underlying.trace(message.value, cause)

  def trace(message: LogSecureString, args: Any*): Unit = underlying.trace(message.value, args)

  def trace(marker: Marker, message: LogSecureString): Unit = underlying.trace(marker, message.value)

  def trace(marker: Marker, message: LogSecureString, cause: Throwable): Unit = underlying.trace(marker, message.value, cause)

  def trace(marker: Marker, message: LogSecureString, args: Any*): Unit = underlying.trace(marker, message.value, args)

  def whenTraceEnabled(body: Unit): Unit = underlying.whenTraceEnabled(body)
}

object SecureScalaLogger {
  def apply(underlying: Logger): SecureScalaLogger = new SecureScalaLogger(underlying)

  implicit class SecureScalaLoggerOps(val underlying: Logger) extends AnyVal {
    def secure: SecureScalaLogger = new SecureScalaLogger(underlying)
  }
}

package secure.logging.log4cats

import org.typelevel.log4cats.Logger
import secure.logging.LogSecureString

/** Wrapper around logger that allows to log secure strings ONLY.
  *
  * @param underlying
  *   logger
  * @tparam F
  *   effect type
  */
class SecureLogger[F[_]](private val underlying: Logger[F]) extends AnyVal {
    def error(message: => LogSecureString): F[Unit] = underlying.error(message.value)
    def warn(message: => LogSecureString): F[Unit]  = underlying.warn(message.value)
    def info(message: => LogSecureString): F[Unit]  = underlying.info(message.value)
    def debug(message: => LogSecureString): F[Unit] = underlying.debug(message.value)
    def trace(message: => LogSecureString): F[Unit] = underlying.trace(message.value)

    def error(t: Throwable)(message: => LogSecureString): F[Unit] =
        underlying.error(t)(message.value)

    def warn(t: Throwable)(message: => LogSecureString): F[Unit] = underlying.warn(t)(message.value)

    def info(t: Throwable)(message: => LogSecureString): F[Unit] = underlying.info(t)(message.value)

    def debug(t: Throwable)(message: => LogSecureString): F[Unit] =
        underlying.debug(t)(message.value)

    def trace(t: Throwable)(message: => LogSecureString): F[Unit] =
        underlying.trace(t)(message.value)
}

object SecureLogger {
    def apply[F[_]](implicit logger: SecureLogger[F]): SecureLogger[F] = logger

    implicit class SecureLoggerOps[F[_]](private val logger: Logger[F]) extends AnyVal {
        def secure: SecureLogger[F] = new SecureLogger(logger)
    }
}

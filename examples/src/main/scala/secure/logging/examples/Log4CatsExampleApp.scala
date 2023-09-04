package secure.logging.examples

import cats.effect._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import secure.logging._
import secure.logging.SecureLogger._

object Log4CatsExampleApp extends IOApp{

  implicit val unsafeLogger: SecureLogger[IO] = Slf4jLogger.getLogger[IO].secure
  override def run(args: List[String]): IO[ExitCode] = {
    val user = User.exampleUser
    SecureLogger[IO].info(sl"user: $user is logged securely") *> IO(ExitCode.Success)
  }
}

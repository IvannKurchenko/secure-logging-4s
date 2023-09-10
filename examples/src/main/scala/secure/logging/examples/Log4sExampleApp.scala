package secure.logging.examples

import org.log4s._
import secure.logging._
import secure.logging.log4s.SecureLogger
import secure.logging.log4s.SecureLogger._

object Log4sExampleApp {
  val logger: SecureLogger = getLogger.secure

  def main(args: Array[String]): Unit = {
    val user = User.exampleUser
    logger.info(sl"user: $user is logged securely")
  }
}

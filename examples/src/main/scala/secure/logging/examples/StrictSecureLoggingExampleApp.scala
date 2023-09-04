package secure.logging.examples

import secure.logging._

object StrictSecureLoggingExampleApp extends StrictSecureLogging {
  def main(args: Array[String]): Unit = {
    val user = User.exampleUser
    logger.info(sl"user: $user is logged securely")
  }
}

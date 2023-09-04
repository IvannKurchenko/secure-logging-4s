package secure.logging.examples

import secure.logging._

object LazySecureLoggingExampleApp extends LazySecureLogging {
  def main(args: Array[String]): Unit = {
    val user = User.exampleUser
    logger.info(sl"user: $user is logged securely")
  }
}

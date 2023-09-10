package secure.logging.examples

import secure.logging._
import secure.logging.scalalogging.LazySecureLogging

object LazySecureLoggingExampleApp extends LazySecureLogging {
  def main(args: Array[String]): Unit = {
    val user = User.exampleUser
    logger.info(sl"user: $user is logged securely")
  }
}

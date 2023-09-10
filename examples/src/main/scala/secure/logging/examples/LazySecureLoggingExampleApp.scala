package secure.logging.examples

import secure.logging._
import secure.logging.auto._

object LazySecureLoggingExampleApp {
    def main(args: Array[String]): Unit = {
        class User(val email: String)
        implicit val stringEncoder: LogSecureEncoder[String] = LogSecureEncoder.sha256
        val exampleUser = new User("john.doe@acome.com")

        println(sl"user: $exampleUser is logged securely")
    }
}

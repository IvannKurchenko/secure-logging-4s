package secure.logging.examples

import secure.logging._

class User(val email: String)

object User {
  implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.sha256.contraMap[User](_.email)

  val exampleUser = new User("john.doe@acome.com")
}

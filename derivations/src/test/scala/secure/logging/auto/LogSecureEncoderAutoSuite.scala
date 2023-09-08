package secure.logging.auto

import secure.logging._
import secure.logging.auto._

class LogSecureEncoderAutoSuite extends munit.FunSuite {
  test("derive LogSecureEncoder for case class") {
    case class User(email: String, phone: String)
    implicit val stringHashEncoder: LogSecureEncoder[String] = LogSecureEncoder.sha256

    val user = User("john.doe@acme.com", "1234567890")
    val logLine = sl"user: $user"
    val expected = "user: (email: 36d6de708b54f80f4e673d0a09bc1e21c8fb52b267b9afbe812f8000b1ab9590, phone: c775e7b757ede630cd0aa1113bd102661ab38829ca52a6422ab782862f268646)"
    assertEquals(logLine.value, expected)
  }
}

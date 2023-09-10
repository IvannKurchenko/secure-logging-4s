package secure.logging.auto

import secure.logging._
import secure.logging.auto._

class LogSecureEncoderAutoSuite extends munit.FunSuite {
  test("derive LogSecureEncoder for case class") {
    case class User(email: String, phone: String)
    implicit val stringHashEncoder: LogSecureEncoder[String] = LogSecureEncoder.sha256

    val user = User("john.doe@acme.com", "1234567890")
    val logLine = LogSecureEncoder[User].encode(user)
    val expected = LogSecured("User$1(email: 36d6de708b54f80f4e673d0a09bc1e21c8fb52b267b9afbe812f8000b1ab9590, phone: c775e7b757ede630cd0aa1113bd102661ab38829ca52a6422ab782862f268646)")
    assertEquals(logLine, expected)
  }

  test("encoder not found interpolation") {
    assertNoDiff(
      compileErrors(
        """
          case class User(email: String, phone: String)
          val user = User("john.doe@acme.com", "1234567890")
          LogSecureEncoder[User].encode(user)
          """),
      """|error: Cannot resolve log secure encoder of the type `User`
         |          LogSecureEncoder[User].encode(user)
         |                          ^
         |""".stripMargin
    )
  }
}

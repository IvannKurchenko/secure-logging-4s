package secure.logging

import secure.logging._

class LogSecureStringInterpolationSuite extends munit.FunSuite {
  test("encoder found interpolation") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.sha256.contraMap[User](_.email)

    val user = User("john.doe@acme.com")

    val logLine = sl"user: $user"
    val expected = "user: 36d6de708b54f80f4e673d0a09bc1e21c8fb52b267b9afbe812f8000b1ab9590"
    assertEquals(logLine.value, expected)
  }

  test("encoder not found interpolation") {
    assertNoDiff(
      compileErrors(
        """
          case class User(email: String)
          val user = User("john.doe@acme.com")
          sl"user: $user"
          """),
      """|error:
         |type mismatch;
         | found   : User
         | required: secure.logging.LogSecured; incompatible interpolation method sl
         |          sl"user: $user"
         |          ^
         |""".stripMargin
    )
  }
}

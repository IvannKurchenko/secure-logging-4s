package secure.logging.log4cats

class SecureLoggerSuite extends munit.FunSuite {

  test("secured logger should not accept regular strings") {
    assertNoDiff(
      compileErrors(
        """
          implicit val unsafeLogger: SecureLogger[IO] = Slf4jLogger.getLogger[IO].secure
          SecureLogger[IO].info("regular string")
          """),
      """|error:
         |overloaded method info with alternatives:
         |  (t: Throwable)(message: => secure.logging.LogSecureString): cats.effect.IO[Unit] <and>
         |  (message: => secure.logging.LogSecureString)cats.effect.IO[Unit]
         | cannot be applied to (String)
         |          SecureLogger[IO].info("regular string")
         |                           ^
         |""".stripMargin
    )
  }
}

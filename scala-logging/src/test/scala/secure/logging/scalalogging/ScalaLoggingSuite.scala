package secure.logging.scalalogging

class ScalaLoggingSuite extends munit.FunSuite {
  test("secured logger should not accept regular strings") {
    assertNoDiff(
      compileErrors(
        """
          object StrictSecureLoggingExampleApp extends StrictSecureLogging {
            def main(args: Array[String]): Unit = {
              logger.info("regular string")
            }
          }
          """),
      """|error:
         |type mismatch;
         | found   : String("regular string")
         | required: secure.logging.LogSecureString
         |              logger.info("regular string")
         |                          ^
         |""".stripMargin
    )
  }
}

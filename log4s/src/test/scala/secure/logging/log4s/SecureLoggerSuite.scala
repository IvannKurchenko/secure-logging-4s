package secure.logging.log4s

import org.log4s._
import secure.logging._
import secure.logging.log4s.SecureLogger
import secure.logging.log4s.SecureLogger._

class SecureLoggerSuite extends munit.FunSuite {
  test("secured logger should not accept regular strings") {
    assertNoDiff(
      compileErrors(
        """
        val logger: SecureLogger = getLogger.secure
        logger.info("regular string")
        """),
      """|error:
         |overloaded method info with alternatives:
         |  (msg: => secure.logging.LogSecureString)Unit <and>
         |  (t: Throwable)(msg: => secure.logging.LogSecureString): Unit
         | cannot be applied to (String)
         |        logger.info("regular string")
         |               ^
         |""".stripMargin
    )
  }
}

package secure.logging

import secure.logging._

class CoreSuite extends munit.FunSuite {
  test("sha-256 hash interpolation") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.sha256.contraMap[User](_.email)
    }

    val user = new User("john.doe@acme.com")
    val logLine = sl"user: $user"
    val expected = "user: 36d6de708b54f80f4e673d0a09bc1e21c8fb52b267b9afbe812f8000b1ab9590"
    assertEquals(logLine.value, expected)
  }

  test("sha-512 hash interpolation") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.sha512.contraMap[User](_.email)
    }

    val user = new User("john.doe@acme.com")
    val logLine = sl"user: $user"
    val expected = "user: 9282184f164e8aac5f62f57eaf04aee142ed205e9befceed052b757cfe1ba60da288c3fa38f734d7035e57513ade3a9d4b961e7aa790260d0ee3470c8cf1e146"
    assertEquals(logLine.value, expected)
  }

  test("prefix masking interpolation") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.mask.contraMap[User](_.email)
    }

    val user = new User("john.doe@acme.com")
    val logLine = sl"user: $user"
    val expected = "user: **********cme.com"
    assertEquals(logLine.value, expected)
  }

  test("suffix masking interpolation") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
    }

    val user = new User("john.doe@acme.com")
    val logLine = sl"user: $user"
    val expected = "user: john.do**********"
    assertEquals(logLine.value, expected)
  }
}

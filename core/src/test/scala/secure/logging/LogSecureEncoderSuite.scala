package secure.logging

import secure.logging._

import scala.collection.immutable.{Queue, SortedMap, SortedSet}

class LogSecureEncoderSuite extends munit.FunSuite {
  test("sha-256 hash interpolation") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.sha256.contraMap[User](_.email)
    }

    val user = User("john.doe@acme.com")
    val logLine = sl"user: $user"
    val expected = "user: 36d6de708b54f80f4e673d0a09bc1e21c8fb52b267b9afbe812f8000b1ab9590"
    assertEquals(logLine.value, expected)
  }

  test("sha-512 hash interpolation") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.sha512.contraMap[User](_.email)
    }

    val user = User("john.doe@acme.com")
    val logLine = sl"user: $user"
    val expected = "user: 9282184f164e8aac5f62f57eaf04aee142ed205e9befceed052b757cfe1ba60da288c3fa38f734d7035e57513ade3a9d4b961e7aa790260d0ee3470c8cf1e146"
    assertEquals(logLine.value, expected)
  }

  test("prefix masking interpolation - string shorter then mask") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskPrefix(20).contraMap[User](_.email)
    }

    val user = User("jd@acme.com")
    val logLine = sl"user: $user"
    val expected = "user: ***********"
    assertEquals(logLine.value, expected)
  }

  test("prefix masking interpolation - string longer then mask") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.mask.contraMap[User](_.email)
    }

    val user = User("john.doe@acme.com")
    val logLine = sl"user: $user"
    val expected = "user: **********cme.com"
    assertEquals(logLine.value, expected)
  }

  test("suffix masking interpolation - string shorter then mask") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(20).contraMap[User](_.email)
    }

    val user = User("john.doe@acme.com")
    val logLine = sl"user: $user"
    val expected = "user: *****************"
    assertEquals(logLine.value, expected)
  }

  test("suffix masking interpolation - string longer then mask") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
    }

    val user = User("john.doe@acme.com")
    val logLine = sl"user: $user"
    val expected = "user: john.do**********"
    assertEquals(logLine.value, expected)
  }

  test("manually defined encoder should encode fields in different ways") {
    case class User(email: String, phone: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = {
        sha256.contraMap[User](_.email).prefix("email: ") |+|
          mask.contraMap[User](_.phone).prefix(", phone: ")
      }
    }

    val user = User("john.doe@acme.com", "+1 123 456 789")
    val logLine = sl"user: $user"
    val expected = "user: email: 36d6de708b54f80f4e673d0a09bc1e21c8fb52b267b9afbe812f8000b1ab9590, phone: ********** 789"
    assertEquals(logLine.value, expected)
  }

  test("implicit encoder for array") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
    }

    val users = Array(User("john.doe@acme.com"), User("john.smith@acme.com"))
    assertEquals(sl"users: $users".value, "users: Array(john.do**********, john.smit**********)")
  }

  test("implicit encoder for list") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
    }

    val users: List[User] = List(User("john.doe@acme.com"), User("john.smith@acme.com"))
    assertEquals(sl"users: $users".value, "users: List(john.do**********, john.smit**********)")
  }

  test("implicit encoder for seq") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
    }

    val users = Seq(User("john.doe@acme.com"), User("john.smith@acme.com"))
    assertEquals(sl"users: $users".value, "users: Seq(john.do**********, john.smit**********)")
  }

  test("implicit encoder for vector") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
    }

    val users: Vector[User] = Vector(User("john.doe@acme.com"), User("john.smith@acme.com"))
    assertEquals(sl"users: $users".value, "users: Vector(john.do**********, john.smit**********)")
  }

  test("implicit encoder for queue") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
    }

    val users = Queue(User("john.doe@acme.com"), User("john.smith@acme.com"))
    assertEquals(sl"users: $users".value, "users: Queue(john.do**********, john.smit**********)")
  }

  test("implicit encoder for set") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
    }

    val users = Set(User("john.doe@acme.com"), User("john.smith@acme.com"))
    assertEquals(sl"users: $users".value, "users: Set(john.do**********, john.smit**********)")
  }

  test("implicit encoder for sorted set") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
      implicit val ordering: Ordering[User] = Ordering.by(_.email)
    }

    val users = SortedSet(User("john.doe@acme.com"), User("john.smith@acme.com"))
    assertEquals(sl"users: $users".value, "users: SortedSet(john.do**********, john.sm**********)")
  }

  test("implicit encoder for map") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
    }

    val users: Map[Int, User] = Map(1 -> User("john.doe@acme.com"))
    assertEquals(sl"users: $users".value, "users: Map(1 -> john.do**********)")
  }

  test("implicit encoder for sorted map") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
    }

    val users: Map[Int, User] = SortedMap(1 -> User("john.doe@acme.com"))
    assertEquals(sl"users: $users".value, "users: Map(1 -> john.do**********)")
  }

  test("implicit encoder for option") {
    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
    }

    val presentUser = Some(User("john.doe@acme.com"))
    assertEquals(sl"user: $presentUser".value, "user: Some(john.do**********)")

    val absentUser = Option.empty[User]
    assertEquals(sl"user: $absentUser".value, "user: None")
  }

  test("implicit encoder for either") {

    implicit val stringEncoder: LogSecureEncoder[String] = LogSecureEncoder.maskSuffix(10)

    case class User(email: String)
    object User {
      implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
    }

    val users: Right[String, User] = Right(User("john.doe@acme.com"))
    val logLine = sl"users: $users"
    val expected = "users: Right(john.do**********)"
    assertEquals(logLine.value, expected)
  }
}

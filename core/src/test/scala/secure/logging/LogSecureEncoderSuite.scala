package secure.logging

import secure.logging._
import scala.collection.immutable._

class LogSecureEncoderSuite extends munit.FunSuite {
  test("sha-256 hash interpolation") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.sha256.contraMap[User](_.email)

    val user = User("john.doe@acme.com")
    val actual = LogSecureEncoder[User].encode(user)
    val expected = LogSecured("36d6de708b54f80f4e673d0a09bc1e21c8fb52b267b9afbe812f8000b1ab9590")
    assertEquals(actual, expected)
  }

  test("sha-512 hash interpolation") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.sha512.contraMap[User](_.email)

    val user = User("john.doe@acme.com")
    val actual = LogSecureEncoder[User].encode(user)
    val expected = LogSecured("9282184f164e8aac5f62f57eaf04aee142ed205e9befceed052b757cfe1ba60da288c3fa38f734d7035e57513ade3a9d4b961e7aa790260d0ee3470c8cf1e146")
    assertEquals(actual, expected)
  }

  test("prefix masking interpolation - string shorter then mask") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskPrefix(20).contraMap[User](_.email)

    val user = User("jd@acme.com")
    val actual = LogSecureEncoder[User].encode(user)
    val expected = LogSecured("***********")
    assertEquals(actual, expected)
  }

  test("prefix masking interpolation - string longer then mask") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.mask.contraMap[User](_.email)

    val user = User("john.doe@acme.com")
    val actual = LogSecureEncoder[User].encode(user)
    val expected = LogSecured("**********cme.com")
    assertEquals(actual, expected)
  }

  test("suffix masking interpolation - string shorter then mask") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(20).contraMap[User](_.email)

    val user = User("john.doe@acme.com")
    val actual = LogSecureEncoder[User].encode(user)
    val expected = LogSecured("*****************")
    assertEquals(actual, expected)
  }

  test("suffix masking interpolation - string longer then mask") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)

    val user = User("john.doe@acme.com")
    val actual = LogSecureEncoder[User].encode(user)
    val expected = LogSecured("john.do**********")
    assertEquals(actual, expected)
  }

  test("manually defined encoder should encode fields in different ways") {
    case class User(email: String, phone: String)
    implicit val encoder: LogSecureEncoder[User] = {
      sha256.contraMap[User](_.email).prefix("email: ") |+|
        mask.contraMap[User](_.phone).prefix(", phone: ")
    }

    val user = User("john.doe@acme.com", "+1 123 456 789")
    val actual = LogSecureEncoder[User].encode(user)
    val expected = LogSecured("email: 36d6de708b54f80f4e673d0a09bc1e21c8fb52b267b9afbe812f8000b1ab9590, phone: ********** 789")
    assertEquals(actual, expected)
  }

  test("implicit encoder for array") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)

    val users = Array(User("john.doe@acme.com"), User("john.smith@acme.com"))
    val actual = LogSecureEncoder[Array[User]].encode(users)
    val expected = LogSecured("(john.do**********, john.smit**********)")
    assertEquals(actual, expected)
  }

  test("implicit encoder for list") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)

    val users: List[User] = List(User("john.doe@acme.com"), User("john.smith@acme.com"))
    val actual = LogSecureEncoder[List[User]].encode(users)
    val expected = LogSecured("(john.do**********, john.smit**********)")
    assertEquals(actual, expected)
  }

  test("implicit encoder for seq") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)

    val users = Seq(User("john.doe@acme.com"), User("john.smith@acme.com"))
    val actual = LogSecureEncoder[Seq[User]].encode(users)
    val expected = LogSecured("(john.do**********, john.smit**********)")
    assertEquals(actual, expected)
  }

  test("implicit encoder for vector") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)

    val users: Vector[User] = Vector(User("john.doe@acme.com"), User("john.smith@acme.com"))
    val actual = LogSecureEncoder[Vector[User]].encode(users)
    val expected = LogSecured("(john.do**********, john.smit**********)")
    assertEquals(actual, expected)
  }

  test("implicit encoder for queue") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)

    val users = Queue(User("john.doe@acme.com"), User("john.smith@acme.com"))
    val actual = LogSecureEncoder[Queue[User]].encode(users)
    val expected = LogSecured("(john.do**********, john.smit**********)")
    assertEquals(actual, expected)
  }

  test("implicit encoder for set") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)

    val users = Set(User("john.doe@acme.com"), User("john.smith@acme.com"))
    val actual = LogSecureEncoder[Set[User]].encode(users)
    val expected = LogSecured("(john.do**********, john.smit**********)")
    assertEquals(actual, expected)
  }

  test("implicit encoder for sorted set") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)
    implicit val ordering: Ordering[User] = Ordering.by(_.email)

    val users = SortedSet(User("john.doe@acme.com"), User("john.smith@acme.com"))
    val actual = LogSecureEncoder[SortedSet[User]].encode(users)
    val expected = LogSecured("(john.do**********, john.smit**********)")
    assertEquals(actual, expected)
  }

  test("implicit encoder for map") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)

    val users: Map[Int, User] = Map(1 -> User("john.doe@acme.com"))
    val actual = LogSecureEncoder[Map[Int, User]].encode(users)
    val expected = LogSecured("(1 -> john.do**********)")
    assertEquals(actual, expected)
  }

  test("implicit encoder for sorted map") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)

    val users: Map[Int, User] = SortedMap(1 -> User("john.doe@acme.com"))
    val actual = LogSecureEncoder[Map[Int, User]].encode(users)
    val expected = LogSecured("(1 -> john.do**********)")
    assertEquals(actual, expected)
  }

  test("implicit encoder for option") {
    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)

    val presentUser: Option[User] = Some(User("john.doe@acme.com"))

    val actualPresent = LogSecureEncoder[Option[User]].encode(presentUser)
    val expectedPresent = LogSecured("Some(john.do**********)")
    assertEquals(actualPresent, expectedPresent)

    val absentUser: Option[User] = Option.empty
    val actualAbsent = LogSecureEncoder[Option[User]].encode(absentUser)
    val expectedAbsent = LogSecured("None")
    assertEquals(actualAbsent, expectedAbsent)
  }

  test("implicit encoder for either") {

    case class User(email: String)
    implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.maskSuffix(10).contraMap[User](_.email)

    val right: Either[Int, User] = Right(User("john.doe@acme.com"))
    val actualRight = LogSecureEncoder[Either[Int, User]].encode(right)
    val expectedRight = LogSecured("Right(john.do**********)")
    assertEquals(actualRight, expectedRight)

    val left: Either[Int, User] = Left(1)
    val actualLeft = LogSecureEncoder[Either[Int, User]].encode(left)
    val expectedLeft = LogSecured("Left(1)")
    assertEquals(actualLeft, expectedLeft)
  }

  test("contravariance test") {
    sealed trait UserData
    case class UserEmail(email: String) extends UserData

    implicit val encoder: LogSecureEncoder[UserData] = {
      LogSecureEncoder.maskSuffix(10).contraMap[UserData] {
        case UserEmail(email) => email
      }
    }

    val userData = UserEmail("john.doe@acme.com")
    val actual = LogSecureEncoder[UserData].encode(userData)
    val expected = LogSecured("john.do**********")
    assertEquals(actual, expected)
  }

  test("compilation error for not found encoder") {
    assertNoDiff(
      compileErrors(
        """
          case class User(email: String)
          LogSecureEncoder[User]
          """),
      """|error: Cannot resolve log secure encoder of the type `User`
         |          LogSecureEncoder[User]
         |                          ^
         |""".stripMargin
    )
  }
}

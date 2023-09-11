# secure-logging-4s

## Introduction
Tiny library for safe references logging that might contain sensitive, private or any other data that should not leak in plain form.
For instance: emails, VINs, phone numbers, etc. The library provides possibility to hide such data by hashing or masking it
in a handy way. Heavily inspired by `cats.Show`.

This library is not a new logging framework itself. Instead, it provides a number of wrappers around existing logging 
frameworks that interpolate incoming strings in various ways to make sure that data which unwanted to be logged in 
plain form is not leaked.

## Installation
Add a library to your project:
```scala
resolvers ++= Resolver.sonatypeOssRepos("releases")
resolvers ++= Resolver.sonatypeOssRepos("snapshots")

libraryDependencies ++= Seq(
  "io.github.ivannkurchenko" %% "secure-logging-4s-core" % "0.0.1-SNAPSHOT",
  "io.github.ivannkurchenko" %% "secure-logging-4s-derivations" % "0.0.1-SNAPSHOT",

  /*
   * Wrappers for scalalogging
   */
  "io.github.ivannkurchenko" %% "secure-logging-4s-scala-logging" % "0.0.1-SNAPSHOT",
  
  /*
   * Wrappers for log4s
   */
  "io.github.ivannkurchenko" %% "secure-logging-4s-log4s" % "0.0.1-SNAPSHOT",

  /*
   * Wrappers for log4cats
   */
  "io.github.ivannkurchenko" %% "secure-logging-4s-log4cats" % "0.0.1-SNAPSHOT",
)
```

## Core
The core idea of the library is to replace using plain string and plain interpolation with `sl` (stands for "secure logging")
that ensures that for logged object there is declared special `LogSecureEncoder` in implicit scope.
This encoder provider a function that transforms any `T` object to `LogSecured` that is a wrapper around `String`.
Since, the main focus of the library is to provide secure logging for strings, which usually contain sensitive data,
for other primitive types, like `Int` and `Long`  default encoders are defined. But, for strings it should be declared
explicitly.

### Hashing
To interpolate string in a hashed form you can use `sha256` or `sha512` encoders:
```scala
import secure.logging._

implicit val stringEncoder: LogSecureEncoder[String] = LogSecureEncoder.sha256

val user = "john.doe@acme.com"
sl"user: $user"
```
will produce string like:
```
user: 36d6de708b54f80f4e673d0a09bc1e21c8fb52b267b9afbe812f8000b1ab9590
```

```scala
import secure.logging._

implicit val stringEncoder: LogSecureEncoder[String] = LogSecureEncoder.sha512

val user = "john.doe@acme.com"
sl"user: $user"
```
will produce string like:
```
user: 9282184f164e8aac5f62f57eaf04aee142ed205e9befceed052b757cfe1ba60da288c3fa38f734d7035e57513ade3a9d4b961e7aa790260d0ee3470c8cf1e146
```

This approach is useful when you want to hide certain data, but still being able to troubleshoot particular issues
for reported cases (e.g. specific email, VIN, etc.).

### Masking
Another approach is to mask prefix or suffix.

To mask first 10 characters with asterisks, you can use `mask` encoder:
```scala
import secure.logging._

implicit val stringEncoder: LogSecureEncoder[String] = LogSecureEncoder.mask

val user = "john.doe@acme.com"
sl"user: $user"
```
will produce string like:
```
user: **********cme.com
```

Or, you can mask the last 10 characters:
```scala
import secure.logging._

implicit val stringEncoder: LogSecureEncoder[String] = LogSecureEncoder.maskSuffix(10)

val user = "john.doe@acme.com"
sl"user: $user"
```
will produce string like:
```
user: john.do**********
```

### Deriving encoders
In case you have a case class with sensitive data, you can derive encoder for it:
```scala
import secure.logging._
import secure.logging.auto._

case class User(email: String, phone: String)
implicit val stringHashEncoder: LogSecureEncoder[String] = LogSecureEncoder.sha256

val user = User("john.doe@acme.com", "1234567890")
sl"user: $user"
```
will produce string like:
```
User(email: 36d6de708b54f80f4e673d0a09bc1e21c8fb52b267b9afbe812f8000b1ab9590, phone: c775e7b757ede630cd0aa1113bd102661ab38829ca52a6422ab782862f268646)
```

### Manual composition
In case if you need to have the ability to encode certain fields in different ways, you can compose encoders manually:
```scala
import secure.logging._

case class User(email: String, phone: String)
implicit val encoder: LogSecureEncoder[User] = {
  sha256.contraMap[User](_.email).prefix("email: ") |+|
    mask.contraMap[User](_.phone).prefix(", phone: ")
}

val user = User("john.doe@acme.com", "+1 123 456 789")
sl"user: $user"
```
will produce string like:
```
user: email: 36d6de708b54f80f4e673d0a09bc1e21c8fb52b267b9afbe812f8000b1ab9590, phone: ********** 789
```

### Custom encoders
You can also instantiate own encoder for any type:
```scala
val encoder: LogSecureEncoder[User] = LogSecureEncoder.instance { user =>
  s"User(email: ${user.email.take(5)}..., phone: ${user.phone.takeRight(3)}...)"
}
```
But be careful with what you are logging in this case.

## Supported loggers
As it was mentioned above, the library is not a logging framework itself. Instead, it provides wrappers around existing
libraries with one difference: `logger.*` methods accept only `LogSecureString` as a parameter, so you can't pass plain string there.
This way it ensures that references will be logged with using proper encoders. Hence, you need to use `sl` interpolator
everywhere.

Let's assume that you have a case class with sensitive data:
```scala
class User(val email: String)

object User {
  implicit val encoder: LogSecureEncoder[User] = LogSecureEncoder.sha256.contraMap[User](_.email)
}
```

### scalalogging
If you are using `scalalogging` library, you can use `StrictSecureLogging` or `LazySecureLogging` traits to have secure logging wrapper around:
```scala
import secure.logging._
import secure.logging.scalalogging.StrictSecureLogging

object StrictSecureLoggingExampleApp extends StrictSecureLogging {
    def main(args: Array[String]): Unit = {
        val user = User("john.doe@acome.com")
        logger.info(sl"user: $user is logged securely")
    }
}
```
which will log line like:
```
20:56:42.156 [main] INFO  s.l.e.StrictSecureLoggingExampleApp$ - user: 58fde0e2d0a030c441506b22368153f321f71b5d5228dc623b57fdc67061507d is logged securely
```

### log4s
For `log4s` library you can use `SecureLogger` wrapper:
```scala
import org.log4s._
import secure.logging._
import secure.logging.log4s.SecureLogger
import secure.logging.log4s.SecureLogger._

object Log4sExampleApp {
    val logger: SecureLogger = getLogger.secure

    def main(args: Array[String]): Unit = {
        val user = User("john.doe@acome.com")
        logger.info(sl"user: $user is logged securely")
    }
}
```
which will log line like:
```
21:00:31.304 [main] INFO  s.logging.examples.Log4sExampleApp - user: 58fde0e2d0a030c441506b22368153f321f71b5d5228dc623b57fdc67061507d is logged securely
```

### log4cats 
For `log4cats` library you can use `SecureLogger` wrapper:
```scala
import cats.effect._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import secure.logging._
import secure.logging.log4cats.SecureLogger
import secure.logging.log4cats.SecureLogger._

object Log4CatsExampleApp extends IOApp {

    implicit val unsafeLogger: SecureLogger[IO] = Slf4jLogger.getLogger[IO].secure
    
    override def run(args: List[String]): IO[ExitCode] = {
        val user = User("john.doe@acome.com")
        SecureLogger[IO].info(sl"user: $user is logged securely") *> IO(ExitCode.Success)
    }
}
```
which will log line like:
```
21:01:43.429 [io-compute-2] INFO  s.l.examples.Log4CatsExampleApp - user: 58fde0e2d0a030c441506b22368153f321f71b5d5228dc623b57fdc67061507d is logged securely
```

## Downsides
Everything comes at a cost. In this case, the cost is performance.
Since, plenty of allocations are involved in the process and wrappers don't leverage macros (as of now) such logging could
be slower than plain string interpolation.

Another possible downside: logging exceptions. Since, it's not yet clear how to encode `Throwable` properly it will
be logged in plain form.
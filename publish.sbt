ThisBuild / organization := "io.github.ivannkurchenko"
ThisBuild / organizationName := "io.github.ivannkurchenko"
ThisBuild / organizationHomepage := Some(url("https://github.com/IvannKurchenko"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/IvannKurchenko/secure-logging-4s"),
    "git@github.com:IvannKurchenko/secure-logging-4s.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "IvannKurchenko",
    name = "Ivan Kurchenko",
    email = "ivankurchenko@gmail.com",
    url = url("https://github.com/IvannKurchenko")
  )
)

ThisBuild / description :=
  """
    |Tiny library for safe logging that might contain sensitive,
    |private or any other data that should not leak in plain form.""".stripMargin

ThisBuild / licenses := List(
  "Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")
)
ThisBuild / homepage := Some(url("https://github.com/IvannKurchenko/secure-logging-4s"))

ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true
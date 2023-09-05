import Dependencies._

ThisBuild / scalaVersion     := "2.13.11"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "secure.logging"

lazy val root = (project in file("."))
  .aggregate(core)

lazy val core = (project in file("core"))
  .settings(
    name := "secure-logging-4s-core",
    libraryDependencies ++= Seq(munit % Test)
  )


lazy val derivations = (project in file("derivations"))
  .settings(
    name := "secure-logging-4s-derivations",
    libraryDependencies ++= Seq(
      munit % Test
    )
  )
  .dependsOn(core)

lazy val scalaLogging = (project in file("scala-logging"))
  .settings(
    name := "secure-logging-4s-scala-logging",
    libraryDependencies ++= Seq(
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      munit % Test
    )
  )
  .dependsOn(core)


lazy val log4s = (project in file("log4s"))
  .settings(
    name := "secure-logging-4s-log4s",
    libraryDependencies ++= Seq("org.log4s" %% "log4s" % "1.10.0")
  )
  .dependsOn(core)

lazy val log4cats = (project in file("log4cats"))
  .settings(
    name := "secure-logging-4s-log4cats",
    libraryDependencies ++= Seq("org.typelevel" %% "log4cats-core" % "2.6.0")
  )
  .dependsOn(core)

lazy val examples = (project in file("examples"))
  .settings(
    name := "secure-logging-4s-examples",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.4.7",
      "org.typelevel" %% "log4cats-slf4j" % "2.6.0"
    )
  )
  .dependsOn(core, scalaLogging, log4s, log4cats)

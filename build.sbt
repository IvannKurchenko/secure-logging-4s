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
    libraryDependencies ++= Seq(munit % Test)
  )
  .dependsOn(core)

lazy val scalaLogging = (project in file("scala-logging"))
  .settings(
    name := "secure-logging-4s-scala-logging",
    libraryDependencies ++= Seq(munit % Test)
  )
  .dependsOn(core)

//lazy val munit = "org.scalameta" %% "munit" % "0.7.20" % Test
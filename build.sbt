name := "Test"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % "0.23.1",
  "org.http4s" %% "http4s-blaze-server" % "0.23.1",
  "org.http4s" %% "http4s-blaze-client" % "0.23.1",
  "com.github.pureconfig" %% "pureconfig" % "0.16.0",
  "org.http4s" %% "http4s-circe" % "0.23.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "ch.qos.logback" % "logback-classic" % "1.2.5",
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.3.0",
  "org.typelevel" %% "log4cats-core" % "2.1.1",
  "org.typelevel" %% "log4cats-slf4j" % "2.1.1"
)
mainClass := Some("ru.intfox.watchingcat.Server")
assemblyJarName := "watchingcat.jar"
scalacOptions += "-Ymacro-annotations"
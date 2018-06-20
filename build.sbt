name := "scala-coin"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka"     %% "akka-http-spray-json"  % "10.0.7",
  "com.typesafe.akka" %% "akka-cluster" % "2.5.12",

  "org.scalatest" %% "scalatest" % "3.0.3" % Test,
  "org.mockito" % "mockito-core" % "2.7.22" % Test
)

initialize ~= { _ =>
  val ansi = System.getProperty("sbt.log.noformat", "false") != "true"
  if (ansi) {
    System.setProperty("scala.color", "true")
  }
}

import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val Http4sVersion = "0.20.13"
val CirceVersion = "0.11.2"
val Specs2Version = "4.1.0"
val LogbackVersion = "1.2.3"

lazy val root = (project in file("."))
  .settings(
    name := "http4s-k8s",
    scalacOptions ++= Seq("-Ypartial-unification"),
    libraryDependencies ++= Seq(
//      "io.circe" %% "circe-core" % circeVersion,
//      "io.circe" %% "circe-generic" % circeVersion,
//      "io.circe" %% "circe-parser" % circeVersion,

      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "io.circe"        %% "circe-generic"       % CirceVersion,
//      "io.circe"        %% "circe-java8"         % CirceVersion,
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
      "org.specs2"      %% "specs2-core"         % Specs2Version % "test",
      scalaTest % Test
    )
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
  "-Xfatal-warnings",
)

guardrailTasks in Compile := List(
//  ScalaClient(file("petstore.yaml")),
//  ScalaClient(file("github.yaml"), pkg="com.example.clients.github"),
//  ScalaServer(file("myserver.yaml"), pkg="com.example.server", tracing=true),
//  ScalaModels(file("myserver.yaml"), pkg="com.example.models"),
//  JavaClient(file("github.yaml"), pkg="com.example.clients.github")
  ScalaModels(file("k8s-openapi.json"), pkg="com.soujiro32167.client", framework = "http4s", imports = List("shims._"), tracing = true)
)

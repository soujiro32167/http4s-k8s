ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val Http4sVersion = "0.21.0"
val CirceVersion = "0.13.0"
val Specs2Version = "4.1.0"
val LogbackVersion = "1.2.3"

lazy val root = (project in file("."))
  .settings(
    name := "http4s-k8s",
    libraryDependencies ++= Seq(
//      "io.circe" %% "circe-core" % circeVersion,
//      "io.circe" %% "circe-generic" % circeVersion,
//      "io.circe" %% "circe-parser" % circeVersion,

      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "io.circe"        %% "circe-generic"       % CirceVersion,
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
      "org.scalatest"   %% "scalatest" %    "3.1.1" %           Test
    )
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings",
)

guardrailTasks in Compile := List(
//  ScalaClient(file("petstore.yaml")),
//  ScalaClient(file("github.yaml"), pkg="com.example.clients.github"),
//  ScalaServer(file("myserver.yaml"), pkg="com.example.server", tracing=true),
//  ScalaModels(file("myserver.yaml"), pkg="com.example.models"),
//  JavaClient(file("github.yaml"), pkg="com.example.clients.github")
//  ScalaModels(file("k8s-openapi.json"), pkg="io.sk8s.models", framework = "http4s", imports = List("shims._")),
  ScalaClient(file("k8s-openapi.json"), pkg="io.sk8s.client", framework = "http4s", imports = List("shims._"), tracing = true)
)

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val Http4sVersion = "0.21.0"
val CirceVersion = "0.13.0"
val Specs2Version = "4.1.0"
val LogbackVersion = "1.2.3"
val zioVersion = "1.0.0-RC18-2"

val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-language:higherKinds",
    "-language:postfixOps",
    "-feature",
    "-Xfatal-warnings",
  )
)

lazy val k8sClient = (project in file("modules") / "k8s-client")
  .settings(commonSettings)
  .settings(
    name := "k8s-client",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "io.circe"        %% "circe-generic"       % CirceVersion,
    ),
    guardrailTasks in Compile := List(
      //  ScalaClient(file("petstore.yaml")),
      //  ScalaClient(file("github.yaml"), pkg="com.example.clients.github"),
      //  ScalaServer(file("myserver.yaml"), pkg="com.example.server", tracing=true),
      //  ScalaModels(file("myserver.yaml"), pkg="com.example.models"),
      //  JavaClient(file("github.yaml"), pkg="com.example.clients.github")
      //  ScalaModels(file("k8s-openapi.json"), pkg="io.sk8s.models", framework = "http4s", imports = List("shims._")),
      ScalaClient(file("k8s-openapi.json"), pkg="io.sk8s.client", framework = "http4s", imports = List("shims._"), tracing = true)
    )
  )

lazy val examples = (project in file("modules") / "examples")
  .settings(commonSettings)
  .settings(
    name := "examples",
    libraryDependencies ++= Seq(
      "dev.zio"   %% "zio"          % zioVersion,
      "dev.zio"   %% "zio-interop-cats"    % "2.0.0.0-RC12",
      "dev.zio" %% "zio-test"     % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .dependsOn(k8sClient)

lazy val sk8s = (project in file("."))
  .settings(
    name := "sk8s",
  )
    .aggregate(k8sClient, examples)

// ---------------------------------------------------------------------------
// Lexicon — the constellation's shared wire contracts (build-time only; no
// runtime service). Two published artifacts, one version:
//   lexicon-grpc     — the Apollo object-storage gRPC service stubs (pekko-grpc),
//                      consumed by apollo-storage (server) + its Scala clients.
//   lexicon-messages — the async HermesMQ message contracts (ScalaPB + canonical
//                      JSON), consumed by Artemis / Hephaestus.
// Python equivalents live under python/. Version derived from git tags (dynver).
// ---------------------------------------------------------------------------

ThisBuild / scalaVersion := "3.3.4" // Scala 3 LTS, matching the constellation
ThisBuild / organization := "io.codex"
ThisBuild / organizationName := "Codex"
ThisBuild / licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
// Version derived from git tags via sbt-dynver (no literal committed); a `vX.Y.Z`
// tag publishes `X.Y.Z`. Follows the shared SemVer + protobuf-evolution policy.
ThisBuild / dynverSeparator := "-"

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-source:3.3"
)

// Pinned so the published jar's transitive Pekko versions match apollo-storage's
// (Pekko forbids a mixed-version classpath).
lazy val pekkoVersion = "1.2.0"
lazy val pekkoHttpVersion = "1.2.0"

// Publish both artifacts to GitHub Packages (like HermesMQ). Only exercised by the
// external publish step; `publishLocal` needs none of this.
lazy val githubPackages = Seq(
  publishTo := Some(
    "GitHub Packages" at "https://maven.pkg.github.com/vezril/the-lexicon"
  ),
  credentials += Credentials(
    "GitHub Package Registry",
    "maven.pkg.github.com",
    "vezril",
    sys.env.getOrElse("GITHUB_TOKEN", "")
  )
)

lazy val lexiconGrpc = (project in file("."))
  .enablePlugins(PekkoGrpcPlugin)
  .aggregate(messages)
  .settings(githubPackages)
  .settings(
    name := "lexicon-grpc",
    // Generate the Apollo object-storage SERVER (power API, so handlers receive
    // request Metadata for auth) + CLIENT stubs from the shared .proto.
    pekkoGrpcGeneratedSources := Seq(PekkoGrpc.Server, PekkoGrpc.Client),
    pekkoGrpcCodeGeneratorSettings += "server_power_apis",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-stream" % pekkoVersion,
      "org.apache.pekko" %% "pekko-http" % pekkoHttpVersion,
      "org.apache.pekko" %% "pekko-http-core" % pekkoHttpVersion,
      // Client stubs use pekko-discovery (GrpcClientSettings); align its version.
      "org.apache.pekko" %% "pekko-discovery" % pekkoVersion
    )
  )

// Async message contracts. No gRPC service — pekko-grpc generates just the ScalaPB
// message case classes; scalapb-json4s provides protobuf's canonical JSON encoding
// (the HermesMQ wire format).
lazy val messages = (project in file("messages"))
  .enablePlugins(PekkoGrpcPlugin)
  .settings(githubPackages)
  .settings(
    name := "lexicon-messages",
    pekkoGrpcGeneratedSources := Seq(PekkoGrpc.Client),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-json4s" % "0.12.1",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )

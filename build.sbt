// ---------------------------------------------------------------------------
// Lexicon — the constellation's shared wire contracts (build-time only; no
// runtime service). This module owns the gRPC service contracts: it holds the
// .proto and publishes the generated stubs so apollo-storage (server) and its
// Scala clients all build against ONE definition (design refactor-grpc-into-lexicon).
//
// Codegen mirrors apollo-storage exactly (pekko-grpc 1.1.1, server_power_apis,
// Server+Client, pekko 1.2.0) so the stubs are drop-in for the server that used
// to generate them locally.
// ---------------------------------------------------------------------------

ThisBuild / scalaVersion := "3.3.4" // Scala 3 LTS, matching the constellation
ThisBuild / organization := "io.codex"
ThisBuild / organizationName := "Codex"
ThisBuild / licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
// Version literal for now; a real repo derives this from git tags (sbt-dynver),
// as apollo-storage does. Bumped per the shared SemVer + protobuf-evolution policy.
ThisBuild / version := "0.1.0"

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

lazy val lexiconGrpc = (project in file("."))
  .enablePlugins(PekkoGrpcPlugin)
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
    ),
    // Publish to GitHub Packages (like HermesMQ). Only exercised by the external
    // publish step; `publishLocal` needs none of this.
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

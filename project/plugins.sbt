// gRPC/protobuf codegen — the SAME plugin version apollo-storage uses (1.1.1), so
// the generated Apollo stubs are byte-identical to what the server implemented
// against before the contract moved here.
addSbtPlugin("org.apache.pekko" % "pekko-grpc-sbt-plugin" % "1.1.1")

// Formatting, to match sibling repos' CI.
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.4")

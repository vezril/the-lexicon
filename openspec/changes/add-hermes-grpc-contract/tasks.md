# Tasks: add-hermes-grpc-contract

The producer half of moving HermesMQ's gRPC into Lexicon — a straight move (no API redesign),
mirroring `refactor-grpc-into-lexicon` (Apollo). HermesMQ's `adopt-lexicon-grpc-contracts` is
gated on this being published.

## 1. Host the contract

- [x] 1.1 Copy HermesMQ's `hermes.proto` verbatim into `modules/hermes-grpc/src/main/protobuf/hermesmq/v1/hermes.proto`, preserving `package hermesmq.v1`, `java_package = "me.cference.hermesmq.grpc"`, and `java_multiple_files`
- [x] 1.2 Confirm the full surface is present: `TopicAdminService` (Create/Get/Update/Delete), `PubSubService` (Publish/CreateSubscription/Pull/StreamMessages/Consume/Ack/ModifyAckDeadline), and messages incl. `StreamRequest`, `ConsumeRequest` oneof, and the `ttl_seconds` publish field

## 2. Build the module

- [x] 2.1 Add a `lexicon-hermes-grpc` sbt subproject with `PekkoGrpcPlugin`, `pekkoGrpcGeneratedSources := Seq(PekkoGrpc.Server, PekkoGrpc.Client)`, `pekkoGrpcCodeGeneratorSettings += "server_power_apis"`, and Pekko `1.2.0` + `pekko-http`/`pekko-discovery` (same as `lexicon-grpc`)
- [x] 2.2 Wire it into the aggregate and the GitHub Packages publish settings; `sbt lexiconHermesGrpc/compile` generates + compiles the server power-API traits (`*PowerApi` + handlers) and client stubs
- [x] 2.3 `sbt lexiconHermesGrpc/publishLocal` produces `io.codex:lexicon-hermes-grpc_3:<version>`

## 3. Release & docs

- [x] 3.1 Ensure the release workflow publishes `lexicon-hermes-grpc` alongside the other artifacts on a `vX.Y.Z` tag
- [x] 3.2 README: add a capability row + short section for the Hermes gRPC contract (mirroring the Apollo row); note the `me.cference.hermesmq.grpc` package is preserved
- [ ] 3.3 Cut a release tag so HermesMQ's adopt change can pin the published version

## 4. Verify (the real proof is downstream)

- [x] 4.1 `openspec validate add-hermes-grpc-contract --strict` clean; `sbt compile` green (module generates + compiles)
- [x] 4.2 Sanity-check against a throwaway HermesMQ build (or during the adopt change): the 8 `me.cference.hermesmq.grpc.*` server sources compile against the jar with no source changes — the migration's safety net

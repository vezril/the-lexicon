## Context

Lexicon publishes `lexicon-grpc` (Apollo, `PekkoGrpcPlugin`, server-power-API + client, Pekko `1.2.0`) and `lexicon-messages` (ScalaPB + json4s). HermesMQ defines its gRPC in `hermesmq/server/src/main/protobuf/hermes.proto` — `package hermesmq.v1`, `java_package me.cference.hermesmq.grpc`, `java_multiple_files` — generated locally with pekko-grpc `1.1.1` + `server_power_apis` into `TopicAdminService`/`PubSubService` traits, power-API handlers, and clients. The Apollo migration proved the pattern: copy the proto in (package preserved), generate the same stubs, publish, and the server compiles against the jar with **zero source changes**. This does the same for Hermes.

## Goals / Non-Goals

**Goals:**
- Host HermesMQ's gRPC contract in Lexicon and publish drop-in Scala server-power-API + client stubs.
- Preserve the `hermesmq.v1` / `me.cference.hermesmq.grpc` packages so the HermesMQ server needs no import changes.
- Keep it a straight move — identical RPC methods, messages, and status semantics.

**Non-Goals:**
- Redesigning the Hermes API; the Python Hermes client; HermesMQ's REST API; changing `lexicon-grpc`/`lexicon-messages`.

## Decisions

- **A separate `lexicon-hermes-grpc` subproject** (not folded into `lexicon-grpc`), so Apollo consumers don't pull Hermes stubs and vice-versa — one artifact per service surface, matching the existing per-artifact split. Its `.proto` lives in its own module `protobuf/` dir.
- **Copy the proto verbatim, package preserved.** `hermesmq/v1/hermes.proto` is copied unchanged, keeping `package hermesmq.v1` + `option java_package = "me.cference.hermesmq.grpc"` + `java_multiple_files`. The generated Scala package is therefore `me.cference.hermesmq.grpc.*` — identical to what HermesMQ generates today, so its server code is source-compatible.
- **Same codegen knobs as `lexicon-grpc`.** `PekkoGrpcPlugin`, `pekkoGrpcGeneratedSources := Seq(PekkoGrpc.Server, PekkoGrpc.Client)`, `pekkoGrpcCodeGeneratorSettings += "server_power_apis"` (so `*PowerApi` traits carry `Metadata` for auth — HermesMQ authenticates from metadata), Pekko `1.2.0` + `pekko-http`/`pekko-discovery` aligned.
- **Published as `io.codex %% lexicon-hermes-grpc`** to `maven.pkg.github.com/vezril/the-lexicon`, versioned by the same dynver tag as the other artifacts; a `vX.Y.Z` tag publishes `X.Y.Z`.
- **Validated by compile.** The Scala half is "done" when the module generates + compiles the server power-API trait + client; the true proof is HermesMQ's adopt change compiling its 8 gRPC sources + passing its gRPC suites against the jar (its safety net).

## Risks / Trade-offs

- **Pekko-version pin is load-bearing downstream.** The jar is built against Pekko `1.2.0`; HermesMQ is on `1.1.3`, and Pekko forbids a mixed-version classpath. So HermesMQ's adopt change must bump its whole Pekko stack to `1.2.0`. That cost lives in the consumer change, but it is *caused* by this artifact's Pekko baseline — flagged so the two changes are sequenced with it in mind (same as Apollo, which already runs `1.2.0`).
- **Proto drift during the window.** Between copying the proto and HermesMQ adopting, a change to HermesMQ's local `hermes.proto` would diverge. Mitigation: the copy is verbatim and the adopt change lands promptly after publish; post-adoption, Lexicon is the sole home.
- **Artifact proliferation.** A third Lexicon artifact (`lexicon-hermes-grpc`) is the honest cost of one-artifact-per-surface; acceptable and consistent with the repo's design.
- **`buf` lint still absent** (as for Apollo) — codegen + the consumer's compile currently stand in for schema validation; noted, not blocking.

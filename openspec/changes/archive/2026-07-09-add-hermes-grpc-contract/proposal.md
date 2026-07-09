## Why

Lexicon already owns the async message contracts (`codex.messages.v1`) and Apollo's object-storage gRPC (`apollostorage.grpc`) — the single source of truth for the constellation's wire contracts. **HermesMQ's gRPC API** (topic admin + pub/sub, incl. server-streaming and bidirectional consume) is still defined *inside* `hermesmq`, so any Scala or Python client of Hermes carries its own view of the contract — the same drift risk the async messages and the Apollo API had. This change is the **producer half** of moving Hermes's gRPC into Lexicon: define it here, publish generated stubs, so the HermesMQ server and every Hermes client generate from one pinned definition. It mirrors `refactor-grpc-into-lexicon` (Apollo) exactly.

## What Changes

- Move HermesMQ's `hermes.proto` into Lexicon (a **copy — no API redesign**), preserving the protobuf `package hermesmq.v1` and `option java_package = "me.cference.hermesmq.grpc"` so the move is **source-compatible** for the HermesMQ server (its `me.cference.hermesmq.grpc.*` imports don't change).
- Add a new sbt subproject **`lexicon-hermes-grpc`** (sibling of `lexicon-grpc`/`lexicon-messages`) that generates the Hermes **server power-API + client** stubs with pekko-grpc (`server_power_apis`, so handlers receive request `Metadata` for auth — matching HermesMQ's current codegen).
- Publish `io.codex %% lexicon-hermes-grpc` to GitHub Packages on each `vX.Y.Z` tag, in the same SemVer/protobuf-evolution story as the other artifacts.
- Keep `lexicon-grpc` (Apollo) and `lexicon-messages` unchanged — this is purely additive.

## Capabilities

### New Capabilities
- `hermes-grpc-contract`: The HermesMQ gRPC service definition (`TopicAdminService` + `PubSubService`, package `hermesmq.v1`) hosted in Lexicon and published as generated Scala server-power-API + client stubs (`io.codex %% lexicon-hermes-grpc`), preserving the `me.cference.hermesmq.grpc` package so the move is source-compatible.

### Modified Capabilities
<!-- none — additive: a new artifact alongside lexicon-grpc and lexicon-messages. -->

## Impact

- **Build:** new `lexicon-hermes-grpc` subproject (own `protobuf/` dir) with `PekkoGrpcPlugin`, `pekkoGrpcGeneratedSources := Seq(Server, Client)`, `server_power_apis`, and the same Pekko `1.2.0`/http deps as `lexicon-grpc`; wired into the aggregate + GitHub Packages publish.
- **Proto:** `src/.../protobuf/hermesmq/v1/hermes.proto` copied verbatim from `hermesmq` (services: `TopicAdminService` create/get/update/delete; `PubSubService` publish/createSubscription/pull/**streamMessages**/**consume**/ack/modifyAckDeadline; messages incl. `StreamRequest`, `ConsumeRequest` oneof, TTL `ttl_seconds`). Package/`java_package` preserved.
- **Release:** the artifact publishes on the next tag; the HermesMQ adopt change pins that version.
- **Docs:** README capability row for the Hermes gRPC contract (mirrors the Apollo row).
- **Cross-repo sequencing:** this is the **producer** half; `hermesmq`'s `adopt-lexicon-grpc-contracts` is **gated** on this being published. Out of scope: changing the Hermes API surface (straight move), the Python Hermes client, and HermesMQ's REST API (stays local).

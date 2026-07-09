# Lexicon

The **shared vocabulary of the Codex constellation** — the single source of truth for the
wire contracts every service speaks. Build-time only (a schema + codegen repo), not a runtime
service.

The problem it solves: contracts defined in more than one service, in **two languages**
(Scala: Artemis, Hephaestus · Python: Argus), drift. Lexicon is the **language-neutral
definition** — protobuf — that generates typed stubs for every consumer, so a producer/consumer
mismatch is a **build error, not a runtime surprise**.

## What's here

| Contract kind | Status | Wire |
| --- | --- | --- |
| **gRPC — Apollo object-storage API** (`apollostorage.grpc`), Scala | ✅ jar published to GitHub Packages | binary protobuf (gRPC) |
| **gRPC — Apollo object-storage API**, Python client (grpcio) for Argus | ✅ package in [`python/`](python/); wheel on each Release | binary protobuf (gRPC) |
| **gRPC — HermesMQ API** (`hermesmq.v1`), Scala | ✅ `lexicon-hermes-grpc` jar (server power API + client) | binary protobuf (gRPC) |
| **Async message contracts** (`codex.messages.v1`), Scala | ✅ `lexicon-messages` jar (ScalaPB + json4s) | protobuf canonical JSON (over Hermes) |
| **Async message contracts**, Python | ✅ in the [`python/`](python/) package | protobuf canonical JSON (over Hermes) |

The Apollo gRPC service definition lives at
[`src/main/protobuf/apollostorage/grpc/object_api.proto`](src/main/protobuf/apollostorage/grpc/object_api.proto)
— migrated out of `apollo-storage` so the server and its clients all generate from one source
(design `refactor-grpc-into-lexicon`). The protobuf `package apollostorage.grpc` is **preserved**,
so the move is source-compatible for the server.

The **HermesMQ** gRPC service definition (`TopicAdminService` + `PubSubService`, incl.
server-streaming `StreamMessages` and bidirectional `Consume`) lives at
[`hermes-grpc/src/main/protobuf/hermesmq/v1/hermes.proto`](hermes-grpc/src/main/protobuf/hermesmq/v1/hermes.proto)
— migrated out of `hermesmq` (change `add-hermes-grpc-contract`). The protobuf `package hermesmq.v1`
and `java_package me.cference.hermesmq.grpc` are **preserved**, so it is source-compatible for the
HermesMQ server. It publishes as its own artifact, `io.codex %% lexicon-hermes-grpc`, so an Apollo
consumer never pulls Hermes stubs and vice-versa. HermesMQ adopts it via its
`adopt-lexicon-grpc-contracts` change.

## Build

Codegen mirrors `apollo-storage` exactly — the same pekko-grpc plugin (`1.1.1`),
`server_power_apis`, `Server` + `Client` sources, and Pekko `1.2.0` — so the stubs are drop-in
for the server that used to generate them locally.

```bash
sbt compile        # generate + compile the Scala gRPC stubs (server power API + client)
sbt publishLocal   # publish io.codex:lexicon-grpc_3:<version> to ~/.ivy2/local
```

## Consuming (JVM)

```scala
// build.sbt
libraryDependencies += "io.codex" %% "lexicon-grpc" % "0.1.0"
```

The jar carries the generated `apollostorage.grpc.*` messages, the `ObjectApiPowerApi` server
trait (+ `ObjectApiPowerApiHandler`), the `ObjectApiClient`, and the service descriptors for
reflection. The consumer implements the trait; it runs no Apollo-service codegen of its own.
`apollo-storage`'s `adopt-lexicon-grpc-contracts` change does exactly this.

## Message contracts (async, over Hermes)

The cross-service async messages — `ProcessMediaJob`, `MediaProcessed`, `MediaFailed`, `TagJob`,
`TagSuggestions` (package `codex.messages.v1`), with `catalog.events` reserved for Ariadne — are
defined in [`messages/`](messages/src/main/protobuf/codex/messages/v1) and published as a separate
artifact, `io.codex %% lexicon-messages`, plus the Python package.

The **wire format on HermesMQ is protobuf's canonical JSON** (camelCase, readable) — the type is
schema-generated but the bytes on the queue are debuggable. Both languages produce byte-identical
JSON for the same message (verified cross-language).

```scala
// Scala (Artemis / Hephaestus) — scalapb-json4s
import scalapb.json4s.{JsonFormat, Parser}
import codex.messages.v1.*
val json = JsonFormat.toJsonString(TagJob(postId = "p", sample = Some(ObjectRef("media", "p/a.jpg"))))
val job  = new Parser().ignoringUnknownFields.fromJsonString[TagJob](json) // tolerant = forward-compatible
```

```python
# Python (Argus) — google.protobuf.json_format
from google.protobuf import json_format
from codex.messages.v1 import tag_pb2 as tag
json = json_format.MessageToJson(tag.TagJob(post_id="p"))
job  = json_format.Parse(json, tag.TagJob(), ignore_unknown_fields=True)  # tolerant = forward-compatible
```

**Forward compatibility is opt-in:** parse with the *tolerant* parser
(`ignoringUnknownFields` / `ignore_unknown_fields=True`) so a consumer on an older minor version
still accepts a message carrying a field added in a newer one.

## Versioning

One SemVer + protobuf-evolution policy for every contract: additive changes (new optional
fields, protobuf-safe) are minor; a breaking change is major; **field numbers are never reused**.
Services pin an exact Lexicon version.

## Roadmap

- `buf` lint / breaking-change checks in CI.
- Adoption in each consumer's repo: Argus (Python), Artemis + Hephaestus (Scala) depend on the
  published Lexicon artifacts instead of hand-maintained copies.

## License

[MIT](LICENSE).

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
| Async message contracts (ProcessMediaJob, MediaProcessed, TagJob, …) | ⏳ planned (`design-lexicon`) | protobuf canonical JSON (over Hermes) |

The Apollo gRPC service definition lives at
[`src/main/protobuf/apollostorage/grpc/object_api.proto`](src/main/protobuf/apollostorage/grpc/object_api.proto)
— migrated out of `apollo-storage` so the server and its clients all generate from one source
(design `refactor-grpc-into-lexicon`). The protobuf `package apollostorage.grpc` is **preserved**,
so the move is source-compatible for the server.

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

## Versioning

One SemVer + protobuf-evolution policy for every contract: additive changes (new optional
fields, protobuf-safe) are minor; a breaking change is major; **field numbers are never reused**.
Services pin an exact Lexicon version.

## Roadmap

- The **async message contracts** (`design-lexicon`) + `buf` lint / breaking-change checks.
- Argus adopts the published Python client (`refactor-grpc-into-lexicon` §3.3, Argus's repo).

## License

[MIT](LICENSE).

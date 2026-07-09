# Change: refactor-grpc-into-lexicon

> **Design capture (explore mode).** Consolidate the gRPC service contracts (Apollo's
> object-storage API) into **Lexicon**, so one repo is the single source of truth for **all**
> protobuf — async messages *and* gRPC APIs. Builds on `design-lexicon`. No code implemented.

## Why

`design-lexicon` moved the async message contracts into Lexicon and left gRPC as "already
protobuf; could live in Lexicon later." This is that later. Apollo's gRPC `.proto` currently
lives in `apollo-storage` and its clients (Artemis, Hephaestus, Argus) each carry their own view
of it — the same drift risk the async contracts had. Bringing the gRPC definitions into Lexicon
makes it the **one home for every wire contract**: server and clients generate from a single,
versioned source, and a mismatch is a build error, not a runtime surprise.

## Decisions carried in from exploration

| Decision | Choice |
|----------|--------|
| Scope | move the **Apollo object-storage gRPC service** (`.proto`: service + request/response messages) into Lexicon |
| Ownership | Lexicon is the source of truth; **Apollo (server) and its clients all generate from it** — contract-first |
| Codegen | extend Lexicon's codegen to emit **gRPC stubs**: Scala server + client (ScalaPB/pekko-grpc) for Apollo + Artemis/Hephaestus; Python client (grpcio) for Argus |
| Versioning | same SemVer + protobuf evolution rules as the message contracts — one versioning story for the whole IDL |
| Wire | gRPC stays binary protobuf on the wire (as gRPC is); only the *definition's home* changes |

## What Changes

- **grpc-contracts** (new): the Apollo object-storage gRPC service + its messages are defined in
  Lexicon as the single source of truth, migrated out of `apollo-storage`.
- **codegen-and-publishing** (adds to the capability): generate gRPC stubs (Scala server/client,
  Python client) from the Lexicon `.proto`, published in the same versioned artifacts as the
  message types.

## Impact

- Affected specs: `grpc-contracts` **ADDED**; `codegen-and-publishing` gains gRPC requirements.
- **apollo-storage** (cross-service): removes its local `.proto`; the server generates its stubs
  from the pinned Lexicon artifact — a coordinated migration. Its earlier scaffolding change
  described the gRPC API inline; that definition now lives in Lexicon.
- Clients: **Artemis** and **Hephaestus** (Scala gRPC client) and **Argus** (Python gRPC client)
  generate their Apollo client stubs from Lexicon — no per-service copy.
- Result: **one protobuf repo for the whole constellation** — async messages + gRPC APIs, one
  codegen/publish pipeline, one versioning story.
- Out of scope: changing the gRPC API surface (a straight move, not a redesign), REST/OpenAPI,
  moving HermesMQ's own API (it stays REST).

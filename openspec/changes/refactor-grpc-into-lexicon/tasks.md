# Tasks: refactor-grpc-into-lexicon

Consolidate the Apollo gRPC contract into Lexicon. A move (no API redesign), coordinated across
apollo-storage + clients.

> **Status (2026-07-09):** the Scala half is **DONE and published**. Repo
> `vezril/the-lexicon` is live; CI (generate+compile) is green; the `v0.1.0` release workflow
> published **`io.codex:lexicon-grpc_3:0.1.0`** (pom+jar+sources+javadoc) to GitHub Packages
> (`maven.pkg.github.com/vezril/the-lexicon`). Verified earlier that apollo-storage compiles and
> passes its gRPC suite (`ObjectApiSpec` 8/8) against the jar with **zero source changes**.
> Remaining: the **Python** client (Argus), `buf` lint, and the async message contracts
> (`design-lexicon`). The Apollo adopt change is now UNBLOCKED (its CI needs a `read:packages`
> token to resolve the artifact — a GitHub Packages Maven quirk even for public packages).

## 1. Define in Lexicon

- [x] 1.1 Copy Apollo's current object-storage `.proto` (service + messages) into Lexicon
  (`src/main/protobuf/apollostorage/grpc/` — pekko-grpc's default source dir)
- [ ] 1.2 buf lint + breaking-change checks; keep the API surface identical to Apollo's current one
  (deferred — buf not yet in the toolchain; codegen+compile currently proves validity)

## 2. Codegen

- [x] 2.1 Generate Scala gRPC **server** stub (ScalaPB/pekko-grpc) + Scala **client** stubs
  (mirrors apollo's pekko-grpc 1.1.1 + `server_power_apis`; proven by compile + apollo consuming it)
- [ ] 2.2 Generate Python gRPC **client** (grpcio) for Argus
- [ ] 2.3 Publish gRPC stubs in the same SemVer Scala jar + Python package as the messages
  (**Scala jar published** to GitHub Packages as `io.codex:lexicon-grpc_3:0.1.0` via the `v0.1.0`
  release workflow; Python package still pending)

## 3. Adopt (coordinated migration)

- [ ] 3.1 apollo-storage: depend on Lexicon for its server stubs; remove its local `.proto`
  (proven via throwaway; the real change `adopt-lexicon-grpc-contracts` is gated on the GH Packages publish)
- [ ] 3.2 Artemis + Hephaestus: use the Lexicon Scala Apollo client stub
- [ ] 3.3 Argus: use the Lexicon Python Apollo client
- [x] 3.4 verify parity: existing Apollo behavior unchanged (a move, not a break)
  (apollo `ObjectApiSpec` 8/8 pass against the Lexicon jar; main + tests compile unchanged)

## 4. Docs

- [ ] 4.1 Lexicon README: one home for messages (JSON wire) + gRPC (binary wire); versioning story

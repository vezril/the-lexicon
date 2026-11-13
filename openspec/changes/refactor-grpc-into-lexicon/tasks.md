# Tasks: refactor-grpc-into-lexicon

Consolidate the Apollo gRPC contract into Lexicon. A move (no API redesign), coordinated across
apollo-storage + clients.

> **Status (2026-11-13):** everything inside this repo is **DONE and published**. Repo
> `vezril/the-lexicon` is live; CI (generate+compile) is green; the release workflow has published
> **`io.codex:lexicon-grpc_3`** (pom+jar+sources+javadoc) to GitHub Packages
> (`maven.pkg.github.com/vezril/the-lexicon`) through **`0.8.0`**, and the Python
> `lexicon_grpc-0.8.0` wheel + sdist are attached to the v0.8.0 GitHub Release (2.3). Verified that
> apollo-storage compiles and passes its gRPC suite (`ObjectApiSpec` 8/8) against the jar with
> **zero source changes**. `buf` lint + breaking-change checks now gate CI (1.2), and the breaking
> check confirms the surface is unchanged back through `v0.7.0` — a move, not a break.
> Remaining work is **all in consumer repos** (3.1–3.3) plus the README (4.1). The Apollo adopt
> change is UNBLOCKED (its CI needs a `read:packages` token to resolve the artifact — a GitHub
> Packages Maven quirk even for public packages).

## 1. Define in Lexicon

- [x] 1.1 Copy Apollo's current object-storage `.proto` (service + messages) into Lexicon
  (`src/main/protobuf/apollostorage/grpc/` — pekko-grpc's default source dir)
- [x] 1.2 buf lint + breaking-change checks; keep the API surface identical to Apollo's current one
  (`buf.yaml` v2 workspace + the `proto` CI job. The Apollo module lints at BASIC on purpose:
  STANDARD wants `apollostorage.grpc.v1`, `ObjectApiService` and per-RPC response types, i.e. the
  renames this change exists to AVOID. `buf breaking` against the latest `v*` tag is what now
  enforces "identical surface" mechanically — it passes clean against v0.7.0 and v0.8.0)

## 2. Codegen

- [x] 2.1 Generate Scala gRPC **server** stub (ScalaPB/pekko-grpc) + Scala **client** stubs
  (mirrors apollo's pekko-grpc 1.1.1 + `server_power_apis`; proven by compile + apollo consuming it)
- [x] 2.2 Generate Python gRPC **client** (grpcio) for Argus
  (`python/` package `lexicon-grpc`, import `apollostorage.grpc`; proven end-to-end — the client
  round-trips CreateBucket/streaming Put+Get/HeadObject/NOT_FOUND against a real apollo-storage
  v0.7.0 container)
- [x] 2.3 Publish gRPC stubs in the same SemVer Scala jar + Python package as the messages
  (DONE at `0.8.0` — one tag, one version, both wires: `Publish Scala jar to GitHub Packages` and
  `Build & attach Python package` both succeeded on the v0.8.0 Release run, and
  `lexicon_grpc-0.8.0-py3-none-any.whl` + `.tar.gz` (hatch-vcs tag-driven) are attached to the
  GitHub Release)

## 3. Adopt (coordinated migration)

- [ ] 3.1 apollo-storage: depend on Lexicon for its server stubs; remove its local `.proto`
  (proven via throwaway; the real change `adopt-lexicon-grpc-contracts` is gated on the GH Packages publish)
- [ ] 3.2 Artemis + Hephaestus: use the Lexicon Scala Apollo client stub
- [ ] 3.3 Argus: use the Lexicon Python Apollo client
- [x] 3.4 verify parity: existing Apollo behavior unchanged (a move, not a break)
  (apollo `ObjectApiSpec` 8/8 pass against the Lexicon jar; main + tests compile unchanged)

## 4. Docs

- [ ] 4.1 Lexicon README: one home for messages (JSON wire) + gRPC (binary wire); versioning story

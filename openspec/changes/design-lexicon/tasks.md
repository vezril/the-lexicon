# Tasks: design-lexicon

Buildout for the shared-contracts repo (build-time only; no runtime service).

> **Status (2026-07-09):** message contracts built + proven. `codex.messages.v1` (media + tag +
> reserved catalog) generate to Scala (ScalaPB) and Python; canonical-JSON round-trip passes in
> both, and the two produce **byte-identical** JSON for the same message (cross-language proof).
> Published as `io.codex:lexicon-messages` (Scala jar) + the Python package on the next release.
> Remaining: `buf` lint (1.3) and per-repo consumer adoption (3.x).

## 0. Scaffold

- [x] 0.1 Repo scaffold (git/GitHub) + CI; README + license (match sibling repos)
- [x] 0.2 Toolchain: ScalaPB (via pekko-grpc) + Python (grpcio/protobuf) — `buf` still deferred (1.3)

## 1. Contracts

- [x] 1.1 `.proto` for ProcessMediaJob · MediaProcessed · MediaFailed · TagJob · TagSuggestions
- [x] 1.2 reserve `catalog.events` (PostCreated · TagsChanged · PostPurged) for the Ariadne extraction
- [ ] 1.3 lint/breaking-change checks (buf) in CI; field numbers never reused (deferred — codegen
      + compile currently guards validity)

## 2. Codegen + publish

- [x] 2.1 Generate Scala (ScalaPB) + Python from the .proto in CI (`lexicon-messages` module +
      `codex.messages.v1` Python package; JSON round-trip tested in both languages)
- [ ] 2.2 Publish SemVer Scala artifact → GitHub Packages; publish versioned Python package
      (wired: root aggregates `messages`, so a `vX.Y.Z` tag publishes `lexicon-messages` too, and
      the Python wheel now includes `codex` — publishes on the next release tag)
- [x] 2.3 Document the JSON-wire encoding + how each service (de)serializes on Hermes (README
      "Message contracts" — canonical JSON + tolerant-parser forward-compat, Scala + Python)

## 3. Adopt

- [ ] 3.1 Artemis + Hephaestus depend on the pinned Scala artifact; Argus on the Python package
      (each in its own repo)
- [ ] 3.2 Update `design-hephaestus-contract` / `design-argus` / `design-artemis-auto-tagging`
      to REFERENCE Lexicon instead of redefining the message shapes

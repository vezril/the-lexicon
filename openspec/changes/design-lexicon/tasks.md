# Tasks: design-lexicon

Buildout for the shared-contracts repo (build-time only; no runtime service).

> **Status (2026-11-13):** everything inside this repo is DONE. `codex.messages.v1` (media + tag +
> reserved catalog) generate to Scala (ScalaPB) and Python; canonical-JSON round-trip passes in
> both, and the two produce **byte-identical** JSON for the same message (cross-language proof).
> Both artifacts are **published**: the `v0.8.0` Release workflow pushed the Scala jars to GitHub
> Packages and attached `lexicon_grpc-0.8.0` (wheel + sdist) to the GitHub Release (2.2). `buf` lint
> + breaking-change checks now gate CI (1.3). Remaining work is **all in consumer repos** (3.x) —
> nothing further is blocked on Lexicon.

## 0. Scaffold

- [x] 0.1 Repo scaffold (git/GitHub) + CI; README + license (match sibling repos)
- [x] 0.2 Toolchain: ScalaPB (via pekko-grpc) + Python (grpcio/protobuf) + `buf` (lint/breaking, 1.3)

## 1. Contracts

- [x] 1.1 `.proto` for ProcessMediaJob · MediaProcessed · MediaFailed · TagJob · TagSuggestions
- [x] 1.2 reserve `catalog.events` (PostCreated · TagsChanged · PostPurged) for the Ariadne extraction
- [x] 1.3 lint/breaking-change checks (buf) in CI; field numbers never reused (`buf.yaml` v2
      workspace over all three proto roots; the `proto` CI job runs `buf lint` then
      `buf breaking --against` the latest `v*` tag, so a retyped or renumbered field fails the
      build — verified by injecting one. `messages` lints at STANDARD; the two inherited gRPC
      surfaces at BASIC, since STANDARD's naming rules would demand consumer-breaking renames)

## 2. Codegen + publish

- [x] 2.1 Generate Scala (ScalaPB) + Python from the .proto in CI (`lexicon-messages` module +
      `codex.messages.v1` Python package; JSON round-trip tested in both languages)
- [x] 2.2 Publish SemVer Scala artifact → GitHub Packages; publish versioned Python package
      (DONE — the `v0.8.0` Release run's `Publish Scala jar to GitHub Packages` and
      `Build & attach Python package` jobs both succeeded; `lexicon_grpc-0.8.0-py3-none-any.whl`
      + `.tar.gz` are attached to the v0.8.0 GitHub Release. Root aggregates `messages`, so the
      tag publishes `lexicon-messages` alongside `lexicon-grpc`/`-hermes-grpc`/`-common`)
- [x] 2.3 Document the JSON-wire encoding + how each service (de)serializes on Hermes (README
      "Message contracts" — canonical JSON + tolerant-parser forward-compat, Scala + Python)

## 3. Adopt

- [ ] 3.1 Artemis + Hephaestus depend on the pinned Scala artifact; Argus on the Python package
      (each in its own repo)
- [ ] 3.2 Update `design-hephaestus-contract` / `design-argus` / `design-artemis-auto-tagging`
      to REFERENCE Lexicon instead of redefining the message shapes

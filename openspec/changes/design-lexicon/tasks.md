# Tasks: design-lexicon

Buildout for the shared-contracts repo (build-time only; no runtime service).

## 0. Scaffold

- [ ] 0.1 Repo scaffold (git/GitHub) + CI; README + license (match sibling repos)
- [ ] 0.2 Toolchain: buf (or protoc) + ScalaPB + a Python protobuf/betterproto generator

## 1. Contracts

- [ ] 1.1 `.proto` for ProcessMediaJob · MediaProcessed · MediaFailed · TagJob · TagSuggestions
- [ ] 1.2 reserve `catalog.events` (PostCreated · TagsChanged · PostPurged) for the Ariadne extraction
- [ ] 1.3 lint/breaking-change checks (buf) in CI; field numbers never reused

## 2. Codegen + publish

- [ ] 2.1 Generate Scala (ScalaPB) + Python from the .proto in CI
- [ ] 2.2 Publish SemVer Scala artifact → GitHub Packages; publish versioned Python package
- [ ] 2.3 Document the JSON-wire encoding + how each service (de)serializes on Hermes

## 3. Adopt

- [ ] 3.1 Artemis + Hephaestus depend on the pinned Scala artifact; Argus on the Python package
- [ ] 3.2 Update `design-hephaestus-contract` / `design-argus` / `design-artemis-auto-tagging`
      to REFERENCE Lexicon instead of redefining the message shapes

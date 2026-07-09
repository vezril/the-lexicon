# Change: design-lexicon

> **Design capture (explore mode).** Lexicon — the single source of truth for the constellation's
> cross-service message contracts. A schema + codegen repo (not a runtime service). No code
> implemented by this change.

## Why

The async HermesMQ message schemas are defined in more than one service and in **two languages**
(Scala: Artemis, Hephaestus · Python: Argus), so they can drift. A shared Scala library can't fix
it (Python can't use it) — the fix needs a **language-neutral definition** that generates types
for every consumer. Lexicon is that definition; drift becomes a compile/type error, not a silent
runtime mismatch.

## Decisions carried in from exploration

| Decision | Choice |
|----------|--------|
| Name | **Lexicon** — the shared vocabulary; a concept-name tier with Codex (deities are reserved for services) |
| IDL | **Protobuf** — one IDL for gRPC + async; already in the stack (Apollo gRPC → ScalaPB in Scala, protobuf in Argus's Python); strong backward-compatible evolution (field numbers) |
| Wire | protobuf's **canonical JSON encoding** over Hermes — readable/debuggable (binary available but unneeded at homelab scale) |
| Distribution | Lexicon CI **generates + publishes** a Scala artifact (GitHub Packages, like Hermes) + a Python package; services pin a **SemVer** version |
| Scope | **async messages now**; the Apollo gRPC contract is already protobuf; the Muses↔Artemis REST contract (OpenAPI) is a separate future formalization |

## What Changes

- **message-contracts** (new): the protobuf definitions for `ProcessMediaJob`, `MediaProcessed`,
  `MediaFailed`, `TagJob`, `TagSuggestions` (and reserved `catalog.events`), with the JSON-wire
  mapping and SemVer + protobuf evolution rules.
- **codegen-and-publishing** (new): generate Scala (ScalaPB) + Python from the `.proto`; publish a
  versioned Scala artifact + Python package; services depend on a pinned version.

## Impact

- Affected specs: `message-contracts`, `codegen-and-publishing` are **ADDED**.
- Repo `the-lexicon` (schema + codegen; the concept name is **Lexicon**). Consumed by **Artemis**,
  **Hephaestus** (Scala artifact) and **Argus** (Python package); the definitions here supersede
  the ad-hoc message schemas currently described inline in `design-hephaestus-contract`,
  `design-argus`, `design-artemis-auto-tagging` (those become references to Lexicon).
- Reuses protobuf tooling every service already needs for Apollo gRPC — no new technology.
- Out of scope: a runtime service (Lexicon is build-time only), the REST/OpenAPI contract,
  binary wire encoding.

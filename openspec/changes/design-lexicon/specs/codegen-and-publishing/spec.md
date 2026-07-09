# codegen-and-publishing

Generating per-language types from the protobuf definitions and publishing versioned artifacts
that services depend on.

## ADDED Requirements

### Requirement: Generate Scala and Python types

Lexicon SHALL generate types for every consumer language from the `.proto` source: **Scala**
(ScalaPB) for Artemis and Hephaestus, and **Python** for Argus. Generation SHALL be reproducible
from the source in CI (no hand-edited generated code checked in as the source of truth).

#### Scenario: Both languages generate from the same source
- **GIVEN** the Lexicon `.proto` definitions
- **WHEN** CI runs codegen
- **THEN** it produces Scala types (ScalaPB) and Python types from the identical source

#### Scenario: Edge case — a malformed proto fails the build
- **GIVEN** an invalid `.proto` change
- **WHEN** CI runs
- **THEN** codegen fails the build (the bad contract never publishes)

### Requirement: Publish versioned artifacts consumers pin

On release, Lexicon SHALL publish a **SemVer-versioned Scala artifact** (to GitHub Packages, as
HermesMQ does) and a **versioned Python package**, so Artemis/Hephaestus (Scala) and Argus
(Python) depend on a **pinned** Lexicon version. A mismatch SHALL surface as a build/type error,
not a runtime surprise.

#### Scenario: A service pins a contract version
- **GIVEN** Lexicon `1.2.0` published
- **WHEN** Artemis depends on `1.2.0` and Argus on the matching Python `1.2.0`
- **THEN** both are built against the same contract version

#### Scenario: Edge case — a producer/consumer version drift is caught at build
- **GIVEN** a producer built against a contract version incompatible with a consumer's pinned version
- **WHEN** the consumer is built (or types are checked)
- **THEN** the incompatibility is a compile/type error, caught before deploy rather than at runtime

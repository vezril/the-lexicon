# message-contracts

The protobuf definitions of the cross-service async messages, with a JSON wire mapping and
backward-compatible evolution rules.

## ADDED Requirements

### Requirement: Protobuf definitions for the async messages

Lexicon SHALL define, in protobuf, the cross-service message contracts — `ProcessMediaJob`,
`MediaProcessed`, `MediaFailed`, `TagJob`, `TagSuggestions` — as the single source of truth,
with `catalog.events` reserved for the future Ariadne extraction. Each service SHALL use the
generated types rather than a hand-maintained copy.

#### Scenario: One definition, many consumers
- **GIVEN** `MediaProcessed` defined once in Lexicon
- **WHEN** Artemis (Scala) and Hephaestus (Scala) build against Lexicon
- **THEN** both use the generated type from the same definition (no per-service copy to drift)

#### Scenario: Edge case — a Python consumer uses the same definition
- **GIVEN** `TagSuggestions` defined in Lexicon
- **WHEN** Argus (Python) builds against the generated Python package
- **THEN** it uses the same contract as Artemis (Scala), so producer and consumer cannot disagree on shape

### Requirement: JSON wire mapping over HermesMQ

Messages SHALL be exchanged over HermesMQ using protobuf's **canonical JSON encoding**, keeping
the wire human-readable/debuggable (consistent with Hermes's explicit-JSON ethos) while the type
is schema-generated. Binary encoding MAY be used later but is not required.

#### Scenario: A message on the queue is readable JSON
- **GIVEN** a `TagJob` published to `media.tag`
- **WHEN** the raw Hermes message is inspected
- **THEN** it is canonical JSON matching the protobuf definition (readable), and consumers parse it into the generated type

### Requirement: Backward-compatible evolution

Contract evolution SHALL follow protobuf rules and SemVer: additive changes (new optional fields)
are minor and backward-compatible; field numbers SHALL never be reused; breaking changes are a
major version bump. Consumers pinned to a compatible version SHALL keep working across minor bumps.

#### Scenario: Adding an optional field is non-breaking
- **GIVEN** a new optional field added to `MediaProcessed` (minor bump)
- **WHEN** a consumer pinned to the previous minor version receives a message with the new field
- **THEN** it still parses successfully (ignores the unknown field), no rebuild required

#### Scenario: Edge case — a breaking change requires a major bump
- **GIVEN** a field's meaning or type must change incompatibly
- **WHEN** the contract is revised
- **THEN** it is released as a major version (field numbers not reused), and consumers migrate deliberately

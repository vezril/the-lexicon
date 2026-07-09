# grpc-contracts

The gRPC service contracts live in Lexicon alongside the async message contracts, making it the
single source of truth for every wire contract in the constellation.

## ADDED Requirements

### Requirement: Apollo gRPC service defined in Lexicon

Lexicon SHALL define the Apollo object-storage gRPC **service** (its RPC methods and their
request/response messages) in protobuf as the single source of truth, migrated out of
`apollo-storage`. The Apollo **server** and every **client** (Artemis, Hephaestus, Argus) SHALL
generate from this one definition rather than a per-service copy. The migration SHALL preserve the
existing API surface (a move, not a redesign).

#### Scenario: Server and clients share one definition
- **GIVEN** the Apollo gRPC service defined in Lexicon
- **WHEN** Apollo (server) and Artemis (client) build against Lexicon
- **THEN** both use stubs generated from the same definition, so they cannot disagree on the API shape

#### Scenario: Edge case — the API surface is unchanged by the move
- **GIVEN** Apollo's current gRPC API
- **WHEN** its definition is moved into Lexicon
- **THEN** the RPC methods and message shapes are identical (existing clients behave the same) — the change is where the `.proto` lives, not what it says

### Requirement: One protobuf home for messages and gRPC

Lexicon SHALL hold both the async message contracts and the gRPC service contracts under one
versioning + evolution policy, so the whole constellation's wire contracts have a single source of
truth and a single release story.

#### Scenario: Both contract kinds evolve under one policy
- **GIVEN** async messages and the Apollo gRPC service both in Lexicon
- **WHEN** a compatible change is released
- **THEN** it follows the same SemVer + protobuf field-number rules for both, in one versioned release

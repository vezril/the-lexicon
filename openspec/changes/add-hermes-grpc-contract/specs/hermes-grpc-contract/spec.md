# hermes-grpc-contract

The HermesMQ gRPC service definition, hosted in Lexicon and published as generated Scala
server-power-API + client stubs, so the HermesMQ server and every Hermes gRPC client generate
from one pinned, versioned source. A straight move of the existing contract — package preserved,
API surface unchanged.

## ADDED Requirements

### Requirement: Host the HermesMQ gRPC contract

Lexicon SHALL host the HermesMQ gRPC service definition (`TopicAdminService` and `PubSubService`,
protobuf `package hermesmq.v1`) as the single source of truth, copied without API change from
`hermesmq`. It SHALL preserve the `hermesmq.v1` protobuf package and the `me.cference.hermesmq.grpc`
`java_package`, so generated Scala types are drop-in for the existing HermesMQ server.

#### Scenario: The contract is defined in Lexicon with the package preserved
- **GIVEN** HermesMQ's current `hermes.proto`
- **WHEN** it is hosted in Lexicon
- **THEN** the protobuf package `hermesmq.v1` and `java_package me.cference.hermesmq.grpc` are unchanged, and all RPCs and messages are present verbatim

#### Scenario: Edge case — the streaming and consume RPCs are included
- **GIVEN** the `PubSubService` definition
- **WHEN** the contract is hosted
- **THEN** it includes `StreamMessages` (server-streaming) and `Consume` (bidirectional) with their `StreamRequest`/`ConsumeRequest` messages, and the `ttl_seconds` publish field — the full current surface

### Requirement: Publish generated Scala stubs

Lexicon SHALL generate and publish the HermesMQ gRPC **server power-API** (handlers receive
request `Metadata`, matching HermesMQ's metadata-based auth) and **client** stubs as a versioned
artifact `io.codex %% lexicon-hermes-grpc` on GitHub Packages, released by the same tag-driven
SemVer flow as the other Lexicon artifacts.

#### Scenario: The module generates the power-API server and client stubs
- **GIVEN** the hosted `hermes.proto`
- **WHEN** the `lexicon-hermes-grpc` module compiles
- **THEN** it produces the `*PowerApi` server traits (with `Metadata`), their handlers, and the client stubs, and the module compiles

#### Scenario: A tagged release publishes the artifact
- **GIVEN** a `vX.Y.Z` tag
- **WHEN** the release workflow runs
- **THEN** `io.codex:lexicon-hermes-grpc_3:X.Y.Z` is published to GitHub Packages

#### Scenario: Edge case — the artifact is independent of the Apollo artifact
- **GIVEN** the existing `lexicon-grpc` (Apollo) artifact
- **WHEN** `lexicon-hermes-grpc` is built and published
- **THEN** it is a separate artifact — an Apollo consumer does not pull Hermes stubs, and vice-versa

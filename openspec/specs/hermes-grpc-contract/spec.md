# hermes-grpc-contract

## Purpose

The HermesMQ gRPC service definition, hosted in Lexicon and published as generated Scala
server-power-API + client stubs, so the HermesMQ server and every Hermes gRPC client generate
from one pinned, versioned source. A straight move of the existing contract — package preserved,
API surface unchanged.
## Requirements
### Requirement: Host the HermesMQ gRPC contract

Lexicon SHALL host the HermesMQ gRPC service definition (`TopicAdminService` and `PubSubService`,
protobuf `package hermesmq.v1`) as the single source of truth, copied without API change from
`hermesmq`. It SHALL preserve the `hermesmq.v1` protobuf package and the `me.cference.hermesmq.grpc`
`java_package`, so generated Scala types are drop-in for the existing HermesMQ server.

The message envelope SHALL carry a **`correlation_id`** field so a request's correlation id can
propagate across service hops via the bus. `Message` SHALL include `string correlation_id` (so a
consumer reads it on delivery) and `PublishRequest` SHALL include `string correlation_id` (so a
producer sets it on publish). Both are **additive** — new field numbers, no existing field renamed
or renumbered. The field is structural only: the contract does not mint, strip, or validate it;
producers set it and consumers adopt it (empty = none supplied).

#### Scenario: The contract is defined in Lexicon with the package preserved
- **GIVEN** HermesMQ's current `hermes.proto`
- **WHEN** it is hosted in Lexicon
- **THEN** the protobuf package `hermesmq.v1` and `java_package me.cference.hermesmq.grpc` are unchanged, and all RPCs and messages are present verbatim

#### Scenario: Edge case — the streaming and consume RPCs are included
- **GIVEN** the `PubSubService` definition
- **WHEN** the contract is hosted
- **THEN** it includes `StreamMessages` (server-streaming) and `Consume` (bidirectional) with their `StreamRequest`/`ConsumeRequest` messages, and the `ttl_seconds` publish field — the full current surface

#### Scenario: A producer sets a correlation id on publish and a consumer reads it back
- **GIVEN** the envelope with `correlation_id`
- **WHEN** a producer publishes with `PublishRequest.correlation_id` set and a consumer pulls the resulting message
- **THEN** the delivered `Message.correlation_id` equals what was published — the bus carried it unchanged (it is never stripped)

#### Scenario: Edge case — the field is additive and back-compatible
- **GIVEN** a client generated before this change
- **WHEN** it talks to a broker built with the new contract
- **THEN** it still works — `correlation_id` is a new field number, absent (empty string) for callers that don't set it, and no existing field moved

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


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

### Requirement: The contract carries subscription start position and layered TTL fields

The hosted contract SHALL expose the fields HermesMQ needs for back-delivery and layered TTL:
`CreateSubscriptionRequest` SHALL carry a start position and an optional TTL, and
`CreateTopicRequest`/`UpdateTopicRequest` SHALL each carry an optional TTL. All SHALL be additive —
new field numbers, nothing renamed or renumbered. The start position SHALL be an enum whose zero
value is unspecified and SHALL mean "from now", so a client generated before this change behaves
exactly as before.

#### Scenario: A gRPC caller can request back-delivery
- **GIVEN** the contract with a start position on subscription creation
- **WHEN** a client creates a subscription with the earliest start position
- **THEN** the request carries that choice, so the broker can back-deliver the topic's retained history

#### Scenario: A client generated before the change is unaffected
- **GIVEN** a client generated from the previous contract
- **WHEN** it creates a subscription against a broker built with this contract
- **THEN** the start position is unspecified, which means "from now" — its previous behaviour exactly

#### Scenario: A gRPC caller can set a topic or subscription TTL
- **GIVEN** the contract with TTL fields on topic create/update and subscription create
- **WHEN** a client sets one
- **THEN** the request carries it, so the broker can resolve expiry across its TTL layers

#### Scenario: Edge case — the semantics the contract cannot enforce are documented in it
- **GIVEN** fields whose meaning depends on broker behaviour the wire format cannot express
- **WHEN** a reader consults the proto
- **THEN** it states that TTL resolution is shortest-wins, that a topic TTL change is forward-looking, and that the earliest start position means earliest *still retained*

### Requirement: Every field settable through the contract is observable through it

A field the contract lets a caller SET SHALL be readable back, and a request a broker may silently
drop SHALL be distinguishable from one it honoured. `Topic` SHALL carry the topic's TTL, and
`CreateSubscriptionResponse` SHALL carry the start position actually applied.

#### Scenario: A caller can read back a topic's TTL
- **GIVEN** a topic whose TTL was set on create or update
- **WHEN** a client calls `GetTopic`, or reads the `Topic` returned by `UpdateTopic`
- **THEN** the TTL is present, so the caller can confirm the value and decide whether to change it

#### Scenario: A silently-downgraded start position is detectable
- **GIVEN** a broker built against a contract that predates `start_from`, which discards it as an unknown field
- **WHEN** a client requests the earliest start position and the broker creates a "from now" subscription
- **THEN** the response's applied start position reads as unspecified rather than earliest, so the client can tell its back-delivery did not happen instead of assuming history it never received

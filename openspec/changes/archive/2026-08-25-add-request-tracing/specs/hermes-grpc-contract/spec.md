# hermes-grpc-contract

Add `correlation_id` to the message envelope so the bus carries request correlation across service
hops — the cross-service propagation the request-tracing standard depends on.

## MODIFIED Requirements

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

# Spec Delta

## ADDED Requirements

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

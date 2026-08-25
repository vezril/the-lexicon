# request-correlation

Lexicon publishes the canonical correlation-id names as shared constants, so every service — Scala
and Python — agrees on one field/header/metadata vocabulary instead of re-declaring strings.

## ADDED Requirements

### Requirement: Publish the canonical correlation names as shared constants

Lexicon SHALL define the canonical request-correlation names **once** and publish them for both
generated languages, so services reference a single source rather than literal strings:

| Purpose | Name |
|---------|------|
| Log field / MDC (JVM) / contextvar (Python) | `correlationId` |
| HTTP header | `X-Correlation-Id` |
| gRPC metadata (lower-case per HTTP/2) | `x-correlation-id` |
| Hermes message envelope field | `correlation_id` |

The Scala artifact SHALL expose these as constants (e.g. `CorrelationNames.{LogField, HttpHeader,
GrpcMeta, Envelope}`) and the Python package SHALL expose the equivalents. The names SHALL be chosen
so `correlationId` maps onto OTel `trace_id` later without a rename (OTel itself is out of scope).

The constants SHALL be published **dependency-free** — no protobuf/gRPC imports — so a pure-HTTP or
non-gRPC consumer can depend on them alone. The module SHALL **document** that `X-Correlation-Id`
(HTTP header, title-case) and `x-correlation-id` (HTTP/2 / gRPC metadata, lower-case) are the same
logical header with casing required per transport, so a consumer does not "normalize" one to the
other. Consumers SHALL reference these constants rather than re-declaring the literals.

#### Scenario: A Scala service references the shared header name
- **GIVEN** the published Scala constants
- **WHEN** Artemis reads/writes the HTTP correlation header
- **THEN** it uses `CorrelationNames.HttpHeader` (= `X-Correlation-Id`) — not a re-declared literal — so a rename is a compile-time change in one place

#### Scenario: A Python service references the same names
- **GIVEN** the published Python constants
- **WHEN** Argus reads the correlation id from a delivered Hermes message and sets its contextvar
- **THEN** it uses the shared envelope name (`correlation_id`) and log-field name (`correlationId`) from Lexicon — identical to what the Scala services use

#### Scenario: A non-gRPC consumer can use the names without pulling grpc/proto
- **GIVEN** a pure-HTTP service that needs the header/log names
- **WHEN** it depends on the constants module
- **THEN** it links only the names — no protobuf/gRPC transitive dependency comes with them

#### Scenario: Edge case — the two casings are documented as intentional
- **GIVEN** `HttpHeader = X-Correlation-Id` and `GrpcMeta = x-correlation-id`
- **WHEN** a developer reads the module
- **THEN** it documents that these are the same logical header at different required casings per transport — so neither is "corrected" to the other

#### Scenario: Edge case — the values are pinned against accidental rename
- **GIVEN** the four canonical names
- **WHEN** the constants module is built
- **THEN** a test asserts the literal values, so a change to any name is deliberate and visible (a single vocabulary across the whole constellation)

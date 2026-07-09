# codegen-and-publishing

Extends Lexicon's codegen to emit gRPC stubs (server + clients) alongside the message types, in
the same versioned artifacts.

## ADDED Requirements

### Requirement: Generate gRPC stubs for server and clients

Lexicon codegen SHALL, from the gRPC service `.proto`, generate the **Scala gRPC server stub**
(ScalaPB / pekko-grpc) for Apollo and **Scala gRPC client stubs** for Artemis and Hephaestus, and
a **Python gRPC client** (grpcio) for Argus — reproducibly in CI from the shared source.

#### Scenario: Server and client stubs come from one source
- **GIVEN** the Apollo gRPC `.proto` in Lexicon
- **WHEN** codegen runs
- **THEN** it produces the Scala server stub (for Apollo) and Scala + Python client stubs (for Artemis/Hephaestus/Argus) from the identical source

#### Scenario: Edge case — a Python-only consumer gets a working client
- **GIVEN** Argus (Python) needs the Apollo client
- **WHEN** it builds against the Lexicon Python package
- **THEN** it has a generated gRPC client matching the same service definition the Scala services use

### Requirement: gRPC stubs published in the versioned artifacts

The generated gRPC stubs SHALL be published in the **same SemVer artifacts** as the message types
(the Scala jar via GitHub Packages, the Python package), so a service pins one Lexicon version for
both its messages and its gRPC contracts, and a producer/consumer mismatch is a build error.

#### Scenario: A service pins one version for everything
- **GIVEN** Lexicon `2.0.0` including messages + the Apollo gRPC service
- **WHEN** Apollo and Artemis both depend on `2.0.0`
- **THEN** they share one pinned version for both the async messages and the gRPC API

#### Scenario: Edge case — a gRPC contract drift is caught at build
- **GIVEN** a client built against a Lexicon version incompatible with the server's
- **WHEN** it is built / type-checked
- **THEN** the incompatibility surfaces as a build/type error, not at runtime

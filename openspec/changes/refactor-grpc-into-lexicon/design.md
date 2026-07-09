# Design: gRPC into Lexicon

Consolidate all protobuf — async messages *and* gRPC APIs — into Lexicon. Captured in explore
mode; no implementation.

## Before / after

```
   BEFORE
     apollo-storage/…/*.proto   Apollo's gRPC API (server + message defs) — owned by Apollo
     clients (Artemis, Hephaestus, Argus) each generate from a copy/view → drift risk
     Lexicon: only the async message contracts

   AFTER
     Lexicon/proto/
        messages/*.proto   ProcessMediaJob · MediaProcessed · TagJob · TagSuggestions · …
        grpc/apollo.proto  the Apollo object-storage SERVICE + its request/response messages
     Apollo (server) + Artemis/Hephaestus (Scala client) + Argus (Python client) ALL generate
     from Lexicon → one definition, no drift
```

## Why contract-first here is fine (even though the .proto leaves Apollo)

Moving Apollo's service definition out of Apollo looks like an inversion — but it's the standard
contract-first pattern, and it's the right call because **multiple parties consume the Apollo
API**: Apollo serves it, and Artemis/Hephaestus/Argus all call it. A shared source of truth beats
"the server owns it, clients chase it." Apollo still *implements* the service; it just generates
its server stubs from the shared definition instead of a private one.

## Codegen (extends design-lexicon's pipeline)

```
   Lexicon .proto  ──┬─ ScalaPB / pekko-grpc → Scala gRPC   SERVER stub → Apollo
                     │                                       CLIENT stub → Artemis, Hephaestus
                     ├─ grpcio/betterproto  → Python gRPC    CLIENT stub → Argus
                     └─ (message types as in design-lexicon) → all services
   published in the same SemVer artifacts (Scala jar + Python package); services PIN a version
```

Same versioning + protobuf evolution rules as the message contracts — one story for the whole
IDL. gRPC stays **binary protobuf on the wire** (as gRPC is); only the definition's *home* moves.
(The async messages keep their protobuf-**JSON** wire over Hermes — the wire encodings differ by
transport, the IDL is unified.)

## Migration (coordinated, low-risk)

```
   1. define the Apollo service .proto in Lexicon (copy of Apollo's current API — no redesign)
   2. Lexicon publishes gRPC stubs (Scala server+client, Python client)
   3. apollo-storage depends on Lexicon for its server stubs; removes its local .proto
   4. Artemis/Hephaestus/Argus depend on Lexicon for the Apollo client stubs
   → a version bump coordinates it; the API surface is unchanged, so it's a move, not a break
```

## Out of scope

Changing the Apollo API surface (straight move) · REST/OpenAPI · HermesMQ's REST API (stays REST).

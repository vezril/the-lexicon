# Proposal

## Why

HermesMQ shipped three capabilities whose REST surface exists and whose gRPC surface cannot, because
the contract lives here:

- **Back-delivery** (`startFrom: earliest`) — a subscription created after a publisher starts
  otherwise silently misses everything already published. This is live over REST.
- **Per-topic TTL** and **per-subscription TTL** — layered TTL resolution, shipped over REST.

So gRPC callers currently cannot ask for behaviour the broker already implements, and they cannot be
told why without reading HermesMQ's roadmap. `ariadne-service` is publishing `product.*` today and
`demeter` is about to write a consumer against it: a gRPC-created subscription silently starting at
`now` is exactly the gap back-delivery was built to close.

## What Changes

- `CreateSubscriptionRequest` gains `start_from` (a new `StartPosition` enum) and `ttl_seconds`.
- `CreateTopicRequest` and `UpdateTopicRequest` gain `ttl_seconds`.
- A `StartPosition` enum whose zero value is `UNSPECIFIED`, treated as `NOW` — so a client generated
  before this change keeps exactly its current behaviour.

All additive: new field numbers, nothing renamed or renumbered, no RPC signature changed.

## Capabilities

### New Capabilities
*(none)*

### Modified Capabilities
- `hermes-grpc-contract`: the hosted contract carries the topic/subscription TTL fields and the
  subscription start position.

## Impact

- **Contract:** three messages gain fields; one enum is added. Back-compatible in both directions —
  an old client omits the fields (zero = previous behaviour), and an old broker ignores them.
- **Consumers:** `hermesmq` bumps its pin and wires the fields through its gRPC services; no other
  service is required to change.
- **Semantics the contract cannot enforce, so they are documented in the proto:** TTL resolution is
  shortest-wins; a topic TTL change is forward-looking; `EARLIEST` means earliest *still retained*
  because snapshot retention purges older journal events.

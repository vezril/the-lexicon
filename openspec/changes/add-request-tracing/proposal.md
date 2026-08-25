# Change: add-request-tracing

> **Constellation foundation.** Land the two things the request-tracing standard needs from the
> single source of truth — the **`correlation_id` envelope field** so Hermes can carry correlation
> across service hops, and the **canonical correlation names** so every service (Scala + Python)
> agrees on one field/header/metadata vocabulary. Enables the per-service `add-request-tracing`
> rollout; Apollo is the JVM reference implementation.

## Why

Apollo shipped request tracing (a `correlationId` on every log line, minted at its trust boundary
and returned to callers), and wrote it up as a **constellation-wide standard**: every service
attaches a correlation id, carries it on every log line, and **propagates** it — so one id stitches
a logical operation across services (Apollo upload → Hermes message → Muses/Argus tagging) and back.

Two pieces are cross-cutting and therefore belong here, in Lexicon, not in any one service:

1. **The Hermes message envelope must carry the id.** The bus is the *vehicle* for cross-service
   correlation — the propagation Apollo's v1 deliberately deferred. A producer sets `correlation_id`
   on publish; the broker carries it verbatim (never strips it); a consumer reads it on delivery and
   adopts it. That only works if `correlation_id` is a **first-class envelope field** in the one
   schema every service generates from.

2. **The names must be defined once.** `correlationId` (log field / MDC / contextvar),
   `X-Correlation-Id` (HTTP), `x-correlation-id` (gRPC metadata), `correlation_id` (envelope). If
   each service re-declares these, a single typo silently breaks correlation and no Loki query
   resolves everywhere. Lexicon is already "the shared vocabulary of the constellation" — these
   names are vocabulary. Names are chosen so `correlationId` ↔ OTel `trace_id` maps later without a
   rename (this is the on-ramp to OTel, not OTel itself).

## Decisions carried in from the standard

| Decision | Choice |
|----------|--------|
| Canonical names | `correlationId` (log/MDC/contextvar) · `X-Correlation-Id` (HTTP) · `x-correlation-id` (gRPC md) · `correlation_id` (envelope) |
| Envelope field | add `correlation_id` to `Message` **and** `PublishRequest` (producer sets, consumer reads, bus carries) |
| Bus contract | Hermes SHALL carry it as first-class, SHALL NOT strip it, exposes it to producers (publish) + consumers (delivery) |
| Trust model | mint-or-adopt at the boundary — external ingress mints (ignores client value); trusted hop (peer call / delivered message) adopts |
| OTel posture | v1 = correlation id only; names map to `trace_id` later. Full W3C Trace Context / spans / backend are **non-goals** |
| Wire | protobuf JSON (unchanged) — `correlation_id` is just another envelope field on the wire |

## What Changes

- **hermes-grpc-contract** (MODIFIED): add `correlation_id` to the `Message` envelope and to
  `PublishRequest`. The bus carries it end-to-end (publish → delivery) and never strips it. A pure
  additive protobuf change — new fields, existing clients unaffected.
- **request-correlation** (ADDED): Lexicon publishes the canonical correlation names as **shared
  constants** for both generated languages (Scala + Python), so services reference one source
  instead of re-declaring strings. This introduces a small hand-written, **dependency-free**
  `lexicon-common` module — the first hand-written *source* in a codegen-only repo (`design.md`,
  Option A, endorsed by Apollo as reference impl: codegen can't emit string constants, so this fills
  that gap rather than being an impurity). It documents the per-transport casing contract
  (`X-Correlation-Id` vs `x-correlation-id`). Still for the the-lexicon session to ratify.

## Impact

- Affected specs: `hermes-grpc-contract` **MODIFIED** (envelope field); `request-correlation`
  **ADDED** (shared names).
- **hermesmq** (cross-service, keystone): the broker must read/store/return `correlation_id` on
  publish and expose it on delivery — its own `add-request-tracing` change is where the bus behaviour
  lands. This change only makes the field *exist* in the contract.
- **All services**: on apply + release, they pin the new Lexicon version to get the envelope field +
  the name constants. Their per-service `add-request-tracing` changes consume them.
- **apollo-storage**: once the envelope field exists, Apollo's small follow-on (adopt-inbound at
  trusted hops + emit `correlation_id` on outbound / published messages) becomes possible — its v1
  mints-only and emits neither.
- Out of scope: any service's runtime behaviour (each per-service change owns that); OTel spans /
  sampling / a trace backend; changing existing message shapes beyond the additive field.

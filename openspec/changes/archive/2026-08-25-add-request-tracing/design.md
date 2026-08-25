# Design — add-request-tracing

## The one real decision: where do the name constants live?

Lexicon is **codegen-only today** — every published symbol is generated from a `.proto`; there is
zero hand-written Scala/Python source. The `correlation_id` **envelope field** fits this perfectly
(it's a message field → protobuf → generated). The **name constants** do not: `X-Correlation-Id`,
`x-correlation-id`, and the `correlationId` MDC/contextvar key are used by *service code* (HTTP
headers, gRPC metadata, log context) — they are not message fields, so protobuf has no natural slot
for them.

Three options:

| Option | What | Trade-off |
|--------|------|-----------|
| **A — hand-written constants module (recommended)** | A tiny `CorrelationNames` object in the Scala artifact + a `correlation.py` in the Python package, published alongside the generated code | One source of truth in code → a typo is a compile error, not a silent break. Introduces the *first* hand-written source in a codegen-only repo (a small, deliberate exception). |
| B — spec-only | Document the names here + in the README; each service defines its own constants | Keeps Lexicon pure codegen, but re-declaration is exactly the drift the standard is trying to kill. |
| C — protobuf enum/const hack | Encode names as enum value names or a message with defaults | Fits codegen, but abuses protobuf; the strings still can't be the *actual* header values cleanly. |

**Recommendation: A** — endorsed by Apollo (the reference impl). Its framing is the clearest
justification: the four names aren't the same kind of thing. `correlation_id` (the envelope field) is
a *true wire contract* — it's in the proto, codegen owns it, nothing to decide. The other three
(`correlationId`, `X-Correlation-Id`, `x-correlation-id`) are *conventions*, not wire schema, and
protobuf codegen literally **cannot** emit string constants — so a hand-written module isn't an
impurity, it's the repo filling the gap codegen can't. Lexicon already ships hand-written build config
and publishes to both languages, so a 4-constant module adds ~no friction. Still flagged for the
the-lexicon session to ratify (it's the first hand-written *source*), but the reference impl concurs.

Two constraints on the module (from Apollo, so centralizing actually pays off):

1. **Dependency-free.** No proto/grpc imports — a pure-HTTP or non-gRPC consumer must be able to
   depend on the constants alone. → its own tiny module (e.g. `lexicon-common`), not folded into
   `lexicon-messages`/`lexicon-hermes-grpc` which pull protobuf/grpc.
2. **Document the casing contract in the module.** `X-Correlation-Id` (HTTP header, title-case) and
   `x-correlation-id` (HTTP/2 / gRPC metadata, lower-case) are the **same logical header** with
   different *required* casing per transport — not a typo. The doc must say so, so nobody "fixes" one
   to match the other.

Consumers then **source** their names from here rather than re-declaring literals (Apollo will switch
its local `CorrelationId.scala` to reference `CorrelationNames`; other services likewise) — that's
what makes this a single source of truth rather than a fourth copy.

### Proposed shape (if A)

Scala — a small dependency-free `lexicon-common` module (no proto/grpc):
```scala
/** Canonical request-correlation names. `X-Correlation-Id` (HTTP header, title-case) and
  * `x-correlation-id` (HTTP/2 / gRPC metadata, lower-case) are the SAME logical header — the
  * casing differs by transport and is required; do not "normalize" one to the other. */
object CorrelationNames {
  val LogField   = "correlationId"     // SLF4J MDC key / JSON log field (→ OTel trace_id later)
  val HttpHeader = "X-Correlation-Id"  // HTTP/1.x header — title-case
  val GrpcMeta   = "x-correlation-id"  // HTTP/2 + gRPC metadata — lower-case (required)
  val Envelope   = "correlation_id"    // Hermes message field (also the proto field name)
}
```
Python (`codex` package) — same names, same casing note in the docstring:
```python
LOG_FIELD   = "correlationId"
HTTP_HEADER = "X-Correlation-Id"   # HTTP/1.x — title-case
GRPC_META   = "x-correlation-id"   # HTTP/2 / gRPC metadata — lower-case (required, not a typo)
ENVELOPE    = "correlation_id"
```

## Protobuf field numbering (envelope)

Additive, back-compatible — new field numbers, nothing renumbered:

- `Message`: `correlation_id` → field **5** (after `publish_time = 4`).
- `PublishRequest`: `correlation_id` → field **6** (after `idempotency_key = 5`).

Consumers read it via `PulledMessage.message.correlation_id`. Under protobuf JSON an absent field
is the empty string — i.e. "no correlation id supplied", which the consumer treats as *mint one*.

## Trust model (why the field is set-by-producer, adopted-by-consumer)

The envelope field is **plumbing**; the *policy* lives in each service:
- A producer publishing a message sets `correlation_id` to its current id (propagation).
- The broker stores + returns it unchanged — it never mints, never strips.
- A consumer adopts the delivered id as its own context (trusted internal hop).
- Empty on delivery → the consumer mints (a message that entered without correlation still gets one).

This keeps Lexicon's part purely structural; anti-injection (external ingress mints its own, ignores
client-supplied ids) is a per-service boundary concern, not the envelope's.

## Non-goals

W3C Trace Context, OTel spans/sampling, a trace backend. Names are chosen to map onto OTel later;
this change ships correlation-id-only.

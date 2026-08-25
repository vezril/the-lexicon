# Tasks — add-request-tracing

## 1. Ratify the design decision
- [x] the-lexicon session: confirm **Option A** (hand-written name constants) vs B/C (`design.md`).

## 2. Envelope field (hermes-grpc-contract)
- [x] Add `string correlation_id = 5;` to `message Message` in `hermes-grpc/src/main/protobuf/hermesmq/v1/hermes.proto` (with a doc comment: carried across hops; bus never strips it).
- [x] Add `string correlation_id = 7;` to `message PublishRequest` (field 7: 6 is `producer_id`, merged after this was drafted) (producer sets on publish; empty = none).
- [x] `sbt hermesGrpc/compile` — regenerate Scala power-API/client stubs; confirm the field is present on `Message`/`PublishRequest`.
- [x] Regenerate the Python bindings — N/A: this repo ships no Python HermesMQ gRPC stubs (the envelope field is a Scala-only contract); Python gets the shared constants (task 3) instead.

## 3. Shared name constants (request-correlation) — Option A (endorsed by Apollo)
- [x] Create a **dependency-free** `lexicon-common` module (no proto/grpc deps) so pure-HTTP consumers can use it.
- [x] Scala: add `CorrelationNames` (LogField / HttpHeader / GrpcMeta / Envelope) with a doc comment on the per-transport casing (`X-Correlation-Id` vs `x-correlation-id` = same logical header).
- [x] Python: add the equivalent constants to the `codex` package, same casing note in the docstring.
- [x] A test asserting the four literal values (guard against accidental rename / casing "fixes").
- [x] Publish the module in both language artifacts (so services can source names instead of re-declaring).

## 4. Release
- [x] Tag a `vX.Y.Z` release (v0.8.0) so `lexicon-hermes-grpc` (+ constants) publish to GitHub Packages / the Python index.
- [x] Note the new version in the rollout (v0.8.0 — services pin it) so per-service `add-request-tracing` changes can pin it.

## 5. Verify
- [x] `openspec validate add-request-tracing`.
- [x] Round-trip: publish a message with a `correlation_id` and confirm a pull returns it unchanged (contract-level; broker behaviour is hermesmq's change).

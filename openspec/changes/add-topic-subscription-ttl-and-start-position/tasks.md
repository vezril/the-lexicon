# Tasks

## 1. Contract

- [x] 1.1 Add `StartPosition` with an `UNSPECIFIED = 0` zero value meaning NOW, and `start_from` + `ttl_seconds` on `CreateSubscriptionRequest`
- [x] 1.2 Add `ttl_seconds` to `CreateTopicRequest` and `UpdateTopicRequest`
- [x] 1.3 Document in the proto the semantics the wire format cannot carry: shortest-wins resolution, forward-looking topic TTL changes, and earliest-still-retained
- [x] 1.4 Verify codegen: `sbt compile` succeeds and the generated Scala types include the new fields

## 2. Follow-up in hermesmq (not this repo)

- [ ] 2.1 Bump the `lexicon-hermes-grpc` pin in `hermesmq`
- [ ] 2.2 Wire `start_from` and both `ttl_seconds` fields through the gRPC services, mapping UNSPECIFIED to NOW
- [ ] 2.3 Remove the "gRPC parity" row from the hermesmq roadmap

## Workflow follow-up

- Archive once the pin bump has landed in hermesmq, so the contract and its only consumer move together.

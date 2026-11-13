# Tasks

## 1. Contract

- [x] 1.1 Add `StartPosition` with an `UNSPECIFIED = 0` zero value meaning NOW, and `start_from` + `ttl_seconds` on `CreateSubscriptionRequest`
- [x] 1.2 Add `ttl_seconds` to `CreateTopicRequest` and `UpdateTopicRequest`
- [x] 1.3 Document in the proto the semantics the wire format cannot carry: shortest-wins resolution, forward-looking topic TTL changes, and earliest-still-retained
- [x] 1.4 Verify codegen: `sbt compile` succeeds and the generated Scala types include the new fields
- [x] 1.5 Make the topic TTL READABLE: `Topic.ttl_seconds` (review follow-up). Settable on
      create/update but absent from `Topic` meant `GetTopic` could not report it and
      `UpdateTopicResponse.topic` would confirm an update while omitting the updated field
- [x] 1.6 Make a dropped `start_from` OBSERVABLE: `CreateSubscriptionResponse.effective_start_from`
      (review follow-up). proto3 discards unknown fields, so a broker predating `start_from`
      silently downgrades EARLIEST to NOW and returns success — the precise failure this change
      exists to prevent, and the guaranteed state until 2.1/2.2 land. Echoing the applied position
      lets a caller tell "honoured" from "silently downgraded"

## 2. Follow-up in hermesmq (not this repo)

- [ ] 2.1 Bump the `lexicon-hermes-grpc` pin in `hermesmq`
- [ ] 2.2 Wire `start_from` and both `ttl_seconds` fields through the gRPC services, mapping UNSPECIFIED to NOW
- [ ] 2.4 Populate `CreateSubscriptionResponse.effective_start_from` with the position actually
      applied, and `Topic.ttl_seconds` on `GetTopic`/`UpdateTopic` — until it does, both read as
      zero, which is exactly the "old broker" signal they are designed to give
- [ ] 2.3 Remove the "gRPC parity" row from the hermesmq roadmap

## Workflow follow-up

- Archive once the pin bump has landed in hermesmq, so the contract and its only consumer move together.

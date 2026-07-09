# Design: Lexicon

The shared vocabulary — cross-service message contracts, defined once. Captured in explore
mode; no implementation.

## Why a neutral IDL (not a shared library)

```
   ProcessMediaJob / MediaProcessed / MediaFailed   Artemis (Scala) ⟷ Hephaestus (Scala)
   TagJob / TagSuggestions                          Artemis (Scala) ⟷ Argus (PYTHON)
   catalog.events (reserved, future)                Artemis (Scala) → Ariadne
```

Two languages → a shared Scala lib can't serve Argus. Lexicon is the **language-neutral
source of truth** that generates types for both.

## Protobuf as the one IDL (already in the stack)

Apollo is gRPC → protobuf, so **Artemis already runs ScalaPB and Argus already carries Python
protobuf** (both are Apollo clients). Lexicon reuses that for the async contracts — one schema
language across gRPC + messaging, with protobuf's field-number rules giving safe evolution.

```
   .proto (in Lexicon)
        ├─ ScalaPB → Scala case classes → Artemis, Hephaestus
        └─ protobuf/betterproto → Python classes → Argus
   wire on Hermes: protobuf CANONICAL JSON (readable) — not binary
```

## The contracts (illustrative)

```
   ProcessMediaJob   jobId · postId · source{bucket,object} · mediaType · contentType · want[]
   MediaProcessed    jobId · postId · status · metadata{w,h,duration?,fps?,md5,filetype,hasAudio?}
                     · phash · derivatives[{kind,ref,width,height,variant?,codec?}] · specVersion
   MediaFailed       jobId · postId · error{code,message} · retriable
   TagJob            postId · sample{bucket,object} · mediaType
   TagSuggestions    postId · suggestions[{tag,category?,confidence,source}] · rating? · status
   catalog.events    (reserved for the Ariadne extraction: PostCreated · TagsChanged · PostPurged)
```

(These consolidate the schemas currently described inline in the Hephaestus/Argus/auto-tagging
designs — those now *reference* Lexicon instead of redefining.)

## Distribution & versioning

```
   Lexicon repo: the .proto + CI that on release:
        ├─ publishes a Scala artifact → GitHub Packages (as HermesMQ already does)
        └─ publishes a Python package  → (registry or index)
   SemVer: additive changes = minor (new optional fields, protobuf-safe) · breaking = major
   services PIN a Lexicon version → a mismatch is a build/type error, never a silent drift
```

## Scope & the broader contract picture

```
   async messages (Hermes)     → Lexicon / protobuf   ← this change
   gRPC API (Apollo)           → protobuf              (already; could also live in Lexicon later)
   REST API (Muses ⟷ Artemis)  → OpenAPI               (separate future formalization; Muses
                                  currently hand-writes a typed client)
```

## Out of scope

A runtime service (Lexicon is build-time only) · binary wire encoding · the REST/OpenAPI
contract · migrating Apollo's existing gRPC .proto into Lexicon (possible later consolidation).

package io.codex.lexicon

/**
 * Canonical request-correlation names (request-correlation) — one source of truth so every service
 * (Scala + Python) agrees on the same field/header/metadata vocabulary and a single typo can't
 * silently break correlation.
 *
 * `X-Correlation-Id` (HTTP header, title-case) and `x-correlation-id` (HTTP/2 / gRPC metadata,
 * lower-case) are the SAME logical header — the casing differs by transport and is REQUIRED (HTTP/2
 * mandates lower-case header names). Do NOT "normalize" one to match the other.
 *
 * Names are chosen so `correlationId` maps onto OTel `trace_id` later without a rename; this is the
 * on-ramp to OTel, not OTel itself. Deliberately dependency-free (no proto/grpc/pekko) so a
 * pure-HTTP or non-gRPC consumer can depend on the constants alone.
 */
object CorrelationNames {
  val LogField: String = "correlationId" // SLF4J MDC key / JSON log field (→ OTel trace_id later)
  val HttpHeader: String = "X-Correlation-Id" // HTTP/1.x header — title-case
  val GrpcMeta: String = "x-correlation-id" // HTTP/2 + gRPC metadata — lower-case (required)
  val Envelope: String = "correlation_id" // Hermes message field (also the proto field name)
}

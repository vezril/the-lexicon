package io.codex.lexicon

import org.scalatest.funsuite.AnyFunSuite

/**
 * Pins the four canonical correlation names to their exact literal values — a guard against an
 * accidental rename or a casing "fix" (`X-Correlation-Id` vs `x-correlation-id` are intentionally
 * different per transport). If one of these changes, correlation silently stops resolving across
 * services, so the values are asserted here, not just documented.
 */
final class CorrelationNamesSpec extends AnyFunSuite {

  test("the canonical names are exactly the shared values") {
    assert(CorrelationNames.LogField == "correlationId")
    assert(CorrelationNames.HttpHeader == "X-Correlation-Id")
    assert(CorrelationNames.GrpcMeta == "x-correlation-id")
    assert(CorrelationNames.Envelope == "correlation_id")
  }

  test("the HTTP header and gRPC metadata are the same header in different casing") {
    assert(CorrelationNames.HttpHeader.equalsIgnoreCase(CorrelationNames.GrpcMeta))
    assert(CorrelationNames.HttpHeader != CorrelationNames.GrpcMeta) // casing differs by transport
    assert(
      CorrelationNames.GrpcMeta == CorrelationNames.GrpcMeta.toLowerCase
    ) // gRPC md must be lower-case
  }
}

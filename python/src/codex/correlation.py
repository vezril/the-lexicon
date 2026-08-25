"""Canonical request-correlation names (request-correlation) — one source of truth so every
service (Scala + Python) agrees on the same field/header/metadata vocabulary and a single typo
can't silently break correlation.

``HTTP_HEADER`` (``X-Correlation-Id``, title-case) and ``GRPC_META`` (``x-correlation-id``,
lower-case) are the SAME logical header — the casing differs by transport and is REQUIRED (HTTP/2
mandates lower-case header names). Do NOT normalize one to match the other.

Names are chosen so ``correlationId`` maps onto OTel ``trace_id`` later without a rename; this is
the on-ramp to OTel, not OTel itself. The mirror of the Scala ``CorrelationNames`` object.
"""

LOG_FIELD = "correlationId"  # log field / MDC key / contextvar (-> OTel trace_id later)
HTTP_HEADER = "X-Correlation-Id"  # HTTP/1.x header - title-case
GRPC_META = "x-correlation-id"  # HTTP/2 / gRPC metadata - lower-case (required, not a typo)
ENVELOPE = "correlation_id"  # Hermes message field (also the proto field name)

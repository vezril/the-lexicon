"""Pin the four canonical correlation names to their exact literals — a guard against an accidental
rename or a casing "fix". These must match the Scala ``CorrelationNames`` object byte-for-byte, or
correlation silently stops resolving across languages."""
from codex import correlation


def test_canonical_names_are_exactly_the_shared_values():
    assert correlation.LOG_FIELD == "correlationId"
    assert correlation.HTTP_HEADER == "X-Correlation-Id"
    assert correlation.GRPC_META == "x-correlation-id"
    assert correlation.ENVELOPE == "correlation_id"


def test_http_header_and_grpc_meta_are_the_same_header_in_different_casing():
    assert correlation.HTTP_HEADER.lower() == correlation.GRPC_META
    assert correlation.HTTP_HEADER != correlation.GRPC_META  # casing differs by transport
    assert correlation.GRPC_META == correlation.GRPC_META.lower()  # gRPC metadata must be lower-case

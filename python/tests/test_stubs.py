"""Server-free checks that the generated Python stubs expose the Apollo contract.
The full cross-language round-trip against a live server is exercised separately."""
from apollostorage.grpc import object_api_pb2 as api
from apollostorage.grpc import object_api_pb2_grpc as api_grpc


def test_service_stubs_present():
    assert hasattr(api_grpc, "ObjectApiStub")
    assert hasattr(api_grpc, "ObjectApiServicer")


def test_message_shapes_match_contract():
    assert [f.name for f in api.CreateBucketRequest.DESCRIPTOR.fields] == ["bucket"]
    meta_fields = [f.name for f in api.ObjectMetadata.DESCRIPTOR.fields]
    assert "generation" in meta_fields and "content_type" in meta_fields


def test_streaming_request_is_a_header_chunk_oneof():
    header = api.PutObjectRequest(header=api.PutHeader(bucket="b", object="o"))
    assert header.WhichOneof("payload") == "header"
    chunk = api.PutObjectRequest(chunk=b"x")
    assert chunk.WhichOneof("payload") == "chunk"

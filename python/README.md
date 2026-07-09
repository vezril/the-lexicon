# lexicon-grpc (Python)

Generated **Python gRPC client** for the Codex constellation's wire contracts — currently
the Apollo object-storage API — from the shared `.proto` in
[the Lexicon](https://github.com/vezril/the-lexicon). Argus (and any Python consumer) builds
its Apollo client from this one source, so it can't drift from the server or the Scala clients.

## Install

Pinned to a Lexicon tag, straight from git (no separate index needed):

```bash
pip install "git+https://github.com/vezril/the-lexicon@v0.2.0#subdirectory=python"
```

Each release also attaches a built wheel/sdist to its GitHub Release.

## Use

```python
import grpc
from apollostorage.grpc import object_api_pb2 as api
from apollostorage.grpc import object_api_pb2_grpc as api_grpc

channel = grpc.insecure_channel("apollo.lan:8443")   # h2c; use secure_channel for TLS
stub = api_grpc.ObjectApiStub(channel)

stub.CreateBucket(api.CreateBucketRequest(bucket="media"))
for chunk in stub.GetObject(api.GetObjectRequest(bucket="media", object="a.jpg")):
    ...  # first message is the header, then payload chunks
```

The generated package mirrors the Scala side's `apollostorage.grpc` package, so the two
languages name the same messages identically.

## Regenerate

Stubs are generated from `../src/main/protobuf/apollostorage/grpc/object_api.proto`:

```bash
python -m grpc_tools.protoc -I ../src/main/protobuf \
  --python_out=src --grpc_python_out=src \
  ../src/main/protobuf/apollostorage/grpc/object_api.proto
```

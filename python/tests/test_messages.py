"""The async message contracts round-trip protobuf's canonical JSON — the HermesMQ
wire format — matching the Scala side byte-for-byte (proven cross-language separately)."""
from google.protobuf import json_format
from codex.messages.v1 import media_pb2 as media
from codex.messages.v1 import tag_pb2 as tag


def test_media_processed_round_trips_canonical_json():
    m = media.MediaProcessed(
        job_id="j", post_id="p", status="ok",
        metadata=media.MediaMetadata(width=800, height=600, md5="d", filetype="png"),
        phash="ab", spec_version=2,
    )
    j = json_format.MessageToJson(m, indent=0)
    assert '"jobId"' in j  # canonical JSON = camelCase
    assert json_format.Parse(j, media.MediaProcessed()) == m


def test_absent_optional_field_is_omitted():
    t = tag.TagSuggestions(post_id="p", status="ok")  # rating (optional) absent
    j = json_format.MessageToJson(t)
    assert "rating" not in j
    assert json_format.Parse(j, tag.TagSuggestions()) == t


def test_tolerant_parse_ignores_unknown_field():
    # Forward-compatibility: a consumer on an older minor version tolerates a new field.
    parsed = json_format.Parse(
        '{"postId":"p","somethingNew":1}', tag.TagSuggestions(), ignore_unknown_fields=True
    )
    assert parsed.post_id == "p"

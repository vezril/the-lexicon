package codex.wire

import codex.messages.v1.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scalapb.json4s.{JsonFormat, Parser}

/**
 * The HermesMQ wire format is protobuf's canonical JSON (design-lexicon). This proves the generated
 * Scala types round-trip through it, that the JSON is the readable camelCase form, and that
 * optional fields are omitted when absent.
 */
final class JsonWireSpec extends AnyWordSpec with Matchers:

  "MediaProcessed" should {
    "round-trip through canonical JSON" in {
      val msg = MediaProcessed(
        jobId = "j-1",
        postId = "p-1",
        status = "ok",
        metadata = Some(
          MediaMetadata(
            width = 1920,
            height = 1080,
            durationSeconds = Some(12.5),
            md5 = "abc",
            filetype = "mp4",
            hasAudio = Some(true)
          )
        ),
        phash = "f00d",
        derivatives = Seq(
          Derivative(
            kind = "thumb",
            ref = Some(ObjectRef("media", "p-1/thumb.webp")),
            width = 320,
            height = 180,
            variant = Some("webp")
          )
        ),
        specVersion = 3
      )
      val json = JsonFormat.toJsonString(msg)
      json should include("\"jobId\"") // canonical JSON = camelCase
      json should include("\"durationSeconds\"")
      JsonFormat.fromJsonString[MediaProcessed](json) shouldBe msg
    }
  }

  "TagSuggestions" should {
    "omit absent optional fields and round-trip" in {
      val msg = TagSuggestions(
        postId = "p-2",
        suggestions = Seq(TagSuggestion(tag = "1girl", confidence = 0.98, source = "wd-tagger-v3")),
        status = "needs_review"
        // rating left absent
      )
      val json = JsonFormat.toJsonString(msg)
      json should not include "rating" // proto3 optional, absent -> omitted
      json should not include "category" // nested optional, absent -> omitted
      JsonFormat.fromJsonString[TagSuggestions](json) shouldBe msg
    }
  }

  "A consumer on an older minor version" should {
    // Forward-compatibility is opt-in: parse with `ignoringUnknownFields` so a message
    // carrying a field added in a newer minor version still parses. Consumers on Hermes
    // SHOULD use this parser (documented in the README wire section).
    "ignore an unknown field when the tolerant parser is used" in {
      val json = """{"postId":"p-3","status":"ok","somethingNew":123}"""
      val parsed = new Parser().ignoringUnknownFields.fromJsonString[TagSuggestions](json)
      parsed.postId shouldBe "p-3"
      parsed.status shouldBe "ok"
    }

    "reject an unknown field under the strict default (opt-in strictness)" in {
      val json = """{"postId":"p-4","somethingNew":1}"""
      an[Exception] should be thrownBy JsonFormat.fromJsonString[TagSuggestions](json)
    }
  }

package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import java.sql.Date
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

class VideoDocumentConverterTest {

    @Test
    fun `converts a video to a document`() {
        val video = TestFactories.createVideoAsset(
            videoId = "5c1786db5236de0001d77747",
            title = "the title",
            description = "the description",
            contentProvider = "the contentPartner",
            contentPartnerVideoId = "the contentPartnerVideoId",
            playbackId = PlaybackId(PlaybackProviderType.KALTURA, "the playbackId"),
            type = LegacyVideoType.NEWS,
            keywords = listOf("keyword1", "keyword2"),
            subjects = setOf(Subject("subject1"), Subject("subject2")),
            releasedOn = LocalDate.ofYearDay(2018, 10),
            duration = Duration.ofSeconds(97),
            legalRestrictions = "legal restrictions"
        )

        val document = VideoDocumentConverter.toDocument(video)

        assertThat(document.getObjectId("_id")).isEqualTo(ObjectId("5c1786db5236de0001d77747"))
        assertThat(document.getString("title")).isEqualTo("the title")
        assertThat(document.getString("description")).isEqualTo("the description")
        assertThat(document.get("source", Map::class.java)).isEqualTo(
            mapOf(
                "contentPartner" to mapOf("name" to "the contentPartner"),
                "videoReference" to "the contentPartnerVideoId"
            )
        )
        assertThat(document.get("playback", Map::class.java)).isEqualTo(
            mapOf(
                "type" to "KALTURA",
                "id" to "the playbackId"
            )
        )
        assertThat(document.get("legacy", Map::class.java)).isEqualTo(
            mapOf(
                "type" to "NEWS"
            )
        )
        assertThat(document.get("keywords", List::class.java)).containsExactly("keyword1", "keyword2")
        assertThat(document.get("subjects", List::class.java)).containsExactlyInAnyOrder("subject1", "subject2")
        assertThat(document.getDate("releaseDate")).isEqualTo(Date.from(ZonedDateTime.of(2018, 1, 10, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()))
        assertThat(document.getInteger("durationSeconds")).isEqualTo(97)
        assertThat(document.getString("legalRestrictions")).isEqualTo("legal restrictions")
    }

    @Test
    fun `converts video to document to video`() {
        val originalVideo = TestFactories.createVideoAsset(videoId = "5ba8e657042ade0001d563fc")

        val document = VideoDocumentConverter.toDocument(originalVideo)
        val restoredVideo = VideoDocumentConverter.fromDocument(document)

        assertThat(restoredVideo).isEqualTo(originalVideo)
    }
}

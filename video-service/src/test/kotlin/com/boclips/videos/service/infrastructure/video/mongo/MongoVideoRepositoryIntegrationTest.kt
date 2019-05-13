package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.*
import com.boclips.videos.service.domain.model.video.LegacySubject
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.Date
import java.util.Locale

class MongoVideoRepositoryIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mongoVideoRepository: MongoVideoRepository

    @Test
    fun `create a video`() {
        val video = TestFactories.createVideo()
        val createdAsset = mongoVideoRepository.create(video)

        assertThat(createdAsset).isEqualToIgnoringGivenFields(video, "playback")
    }

    @Test
    fun `find a video`() {
        val originalAsset = mongoVideoRepository.create(TestFactories.createVideo())
        val retrievedAsset = mongoVideoRepository.find(originalAsset.videoId)!!

        assertThat(retrievedAsset).isEqualTo(originalAsset)
    }

    @Test
    fun `find video when does not exist`() {
        assertThat(mongoVideoRepository.find(VideoId(TestFactories.aValidId()))).isNull()
    }

    @Test
    fun `lookup videos by ids maintains video order`() {
        val id1 = mongoVideoRepository.create(TestFactories.createVideo()).videoId
        val id2 = mongoVideoRepository.create(TestFactories.createVideo()).videoId
        val id3 = mongoVideoRepository.create(TestFactories.createVideo()).videoId

        val videos = mongoVideoRepository.findAll(listOf(id2, id1, id3))

        assertThat(videos.map { it.videoId }).containsExactly(id2, id1, id3)
    }

    @Test
    fun `delete a video`() {
        val id = TestFactories.aValidId()
        val originalAsset = TestFactories.createVideo(videoId = id)

        mongoVideoRepository.create(originalAsset)
        mongoVideoRepository.delete(VideoId(id))

        assertThat(mongoVideoRepository.find(VideoId(id))).isNull()
    }

    @Test
    fun `stream all`() {
        mongoVideoRepository.create(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId(),
                searchable = false
            )
        )
        mongoVideoRepository.create(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId(),
                searchable = true
            )
        )

        var videos: List<Video> = emptyList()

        mongoVideoRepository.streamAll { videos = it.toList() }

        assertThat(videos).hasSize(2)
    }

    @Test
    fun `stream all searchable videos`() {
        mongoVideoRepository.create(TestFactories.createVideo(videoId = TestFactories.aValidId()))
        mongoVideoRepository.create(TestFactories.createVideo(videoId = TestFactories.aValidId()))
        mongoVideoRepository.create(TestFactories.createVideo(videoId = TestFactories.aValidId()))
        mongoVideoRepository.create(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId(),
                searchable = false
            )
        )

        var videos: List<Video> = emptyList()
        mongoVideoRepository.streamAll(VideoFilter.IsSearchable) { videos = it.toList() }

        assertThat(videos).hasSize(3)
    }

    @Test
    fun `stream all by content partner`() {
        mongoVideoRepository.create(TestFactories.createVideo(contentPartnerId = "TED"))
        mongoVideoRepository.create(TestFactories.createVideo(contentPartnerId = "Bob"))
        mongoVideoRepository.create(TestFactories.createVideo(contentPartnerId = "TED"))

        var videos: List<Video> = emptyList()
        mongoVideoRepository.streamAll(VideoFilter.ContentPartnerIs("TED")) { videos = it.toList() }

        assertThat(videos).hasSize(2)
    }

    @Test
    fun `can update playback`() {
        val originalAsset = mongoVideoRepository.create(
            TestFactories.createVideo(
                playback = TestFactories.createKalturaPlayback(playbackId = "ref-id-1")
            )
        )

        mongoVideoRepository.bulkUpdate(
            listOf(
                VideoUpdateCommand.ReplacePlayback(
                    originalAsset.videoId,
                    TestFactories.createKalturaPlayback(
                        duration = Duration.ZERO,
                        playbackId = "ref-123",
                        downloadUrl = "download-url-updated",
                        thumbnailUrl = "thumnbnail-url-updated",
                        hlsStreamUrl = "stream-url-updated",
                        dashStreamUrl = "dash-url-updated"
                    )
                )
            )
        )

        val updatedAsset = mongoVideoRepository.find(originalAsset.videoId)

        assertThat(updatedAsset!!.playback).isNotNull
        assertThat(updatedAsset.playback.id.value).isEqualTo("ref-123")
        assertThat(updatedAsset.playback.thumbnailUrl).isEqualTo("thumnbnail-url-updated")
        assertThat(updatedAsset.playback.duration).isEqualTo(Duration.ZERO)
        assertThat((updatedAsset.playback as StreamPlayback).downloadUrl).isEqualTo("download-url-updated")
        assertThat((updatedAsset.playback as StreamPlayback).appleHlsStreamUrl).isEqualTo("stream-url-updated")
        assertThat((updatedAsset.playback as StreamPlayback).mpegDashStreamUrl).isEqualTo("dash-url-updated")
    }

    @Test
    fun `can update subjects`() {
        val originalAsset = mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "original title",
                subjects = setOf(LegacySubject("Maths"))
            )
        )

        val updatedAsset = mongoVideoRepository.update(
            VideoUpdateCommand.ReplaceSubjects(
                originalAsset.videoId,
                listOf(LegacySubject("Biology"))
            )
        )

        assertThat(updatedAsset).isEqualToIgnoringGivenFields(originalAsset, "subjects")
        assertThat(updatedAsset.subjects).containsOnly(LegacySubject("Biology"))
    }

    @Test
    fun `update throws when video not found`() {
        assertThrows<VideoNotFoundException> {
            mongoVideoRepository.update(
                VideoUpdateCommand.ReplaceDuration(
                    VideoId(value = TestFactories.aValidId()),
                    duration = Duration.ZERO
                )
            )
        }
    }

    @Test
    fun `bulk update applies multiple independent updates at once`() {
        val originalVideo1 = mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "original title 1",
                subjects = setOf(LegacySubject("German")),
                playback = TestFactories.createKalturaPlayback(
                    duration = Duration.ofMinutes(1)
                )
            )
        )

        val originalVideo2 = mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "original title 2",
                playback = TestFactories.createKalturaPlayback(
                    duration = Duration.ofMinutes(99)
                ),
                subjects = setOf(LegacySubject("Maths"))
            )
        )

        val updates = listOf(
            VideoUpdateCommand.ReplaceSubjects(
                videoId = originalVideo1.videoId,
                subjects = emptyList()
            ),
            VideoUpdateCommand.ReplaceDuration(
                videoId = originalVideo1.videoId,
                duration = Duration.ofMinutes(10)
            ),
            VideoUpdateCommand.ReplaceSubjects(
                videoId = originalVideo2.videoId,
                subjects = listOf(LegacySubject("French"))
            ),
            VideoUpdateCommand.ReplaceDuration(
                videoId = originalVideo2.videoId,
                duration = Duration.ofMinutes(11)
            )
        )

        mongoVideoRepository.bulkUpdate(updates)

        val updatedVideo1 = mongoVideoRepository.find(originalVideo1.videoId)!!
        val updatedVideo2 = mongoVideoRepository.find(originalVideo2.videoId)!!

        assertThat(updatedVideo1).isEqualToIgnoringGivenFields(originalVideo1, "subjects", "duration", "playback")
        assertThat(updatedVideo1.playback.duration).isEqualTo(Duration.ofMinutes(10))
        assertThat(updatedVideo1.subjects).isEmpty()

        assertThat(updatedVideo2).isEqualToIgnoringGivenFields(originalVideo2, "subjects", "duration", "playback")
        assertThat(updatedVideo2.playback.duration).isEqualTo(Duration.ofMinutes(11))
        assertThat(updatedVideo2.subjects).isEqualTo(setOf(LegacySubject("French")))
    }

    @Test
    fun `find by content partner and content partner video id`() {
        val video = TestFactories.createVideo(
            videoId = TestFactories.aValidId(),
            contentPartnerVideoId = "ted-id-1",
            contentPartnerId = "TED Talks"
        )

        mongoVideoRepository.create(video)

        assertThat(mongoVideoRepository.existsVideoFromContentPartner("TED Talks", "ted-id-1")).isTrue()
        assertThat(mongoVideoRepository.existsVideoFromContentPartner("TED Talks", "ted-id-2")).isFalse()
        assertThat(mongoVideoRepository.existsVideoFromContentPartner("TED Talks abc", "ted-id-1")).isFalse()
    }

    @Test
    fun `updates searchable`() {
        val video = mongoVideoRepository.create(TestFactories.createVideo(searchable = true))

        mongoVideoRepository.update(VideoUpdateCommand.HideFromSearch(video.videoId))

        assertThat(mongoVideoRepository.find(video.videoId)!!.searchable).isEqualTo(false)

        mongoVideoRepository.update(VideoUpdateCommand.MakeSearchable(video.videoId))

        assertThat(mongoVideoRepository.find(video.videoId)!!.searchable).isEqualTo(true)
    }

    @Test
    fun `replaces language`() {
        val video = mongoVideoRepository.create(TestFactories.createVideo(language = Locale.TAIWAN))

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceLanguage(video.videoId, Locale.GERMAN))

        val updatedAsset = mongoVideoRepository.find(video.videoId)

        assertThat(updatedAsset!!.language).isEqualTo(Locale.GERMAN)
    }

    @Test
    fun `replaces transcript`() {
        val video = mongoVideoRepository.create(TestFactories.createVideo(transcript = null))

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceTranscript(video.videoId, "bla bla bla"))

        val updatedAsset = mongoVideoRepository.find(video.videoId)

        assertThat(updatedAsset!!.transcript).isEqualTo("bla bla bla")
    }

    @Test
    fun `replaces topics`() {
        val video = mongoVideoRepository.create(TestFactories.createVideo(transcript = null))
        val topic = Topic(
            name = "Bayesian Methods",
            language = Locale.US,
            confidence = 0.85,
            parent = Topic(name = "Statistics", confidence = 1.0, language = Locale.US, parent = null)
        )

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceTopics(video.videoId, setOf(topic)))

        val updatedAsset = mongoVideoRepository.find(video.videoId)

        assertThat(updatedAsset!!.topics).containsExactly(topic)
    }

    @Test
    fun `replaces keywords`() {
        val video = mongoVideoRepository.create(TestFactories.createVideo(keywords = listOf("old")))

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceKeywords(video.videoId, setOf("new")))

        val updatedAsset = mongoVideoRepository.find(video.videoId)

        assertThat(updatedAsset!!.keywords).containsExactly("new")
    }

    @Nested
    @DisplayName(value = "Migration Tests")
    inner class MigrationTests {
        @Test
        fun `can retrieve legacy video documents but it is not playable`() {
            mongoClient
                .getDatabase(DATABASE_NAME)
                .getCollection(MongoVideoRepository.collectionName)
                .insertOne(
                    Document()
                        .append("_id", ObjectId("5c55697860fef77aa4af323a"))
                        .append("title", "Mah Video")
                        .append("description", "Ain't no video like this one")
                        .append(
                            "source", Document()
                                .append("contentPartner", Document().append("name", "cp-name"))
                                .append("videoReference", "ref")
                        )
                        .append("playback", Document().append("id", "some-id").append("type", "KALTURA"))
                        .append("legacy", Document().append("type", LegacyVideoType.NEWS.name))
                        .append("keywords", emptyList<String>())
                        .append("subjects", emptyList<String>())
                        .append("releaseDate", Date())
                        .append("durationSeconds", 10)
                        .append("legalRestrictions", "Some restrictions")
                        .append("language", "en")
                        .append("searchable", true)
                )

            val video = mongoVideoRepository.find(VideoId(value = "5c55697860fef77aa4af323a"))

            assertThat(video).isNotNull
            assertThat(video!!.isPlayable()).isFalse()
        }

        @Test
        fun `can parse valid kaltura video and video is playable`() {
            mongoClient
                .getDatabase(DATABASE_NAME)
                .getCollection(MongoVideoRepository.collectionName)
                .insertOne(
                    Document()
                        .append("_id", ObjectId("5c55697860fef77aa4af323a"))
                        .append("title", "Mah Video")
                        .append("description", "Ain't no video like this one")
                        .append(
                            "source", Document()
                                .append("contentPartner", Document().append("name", "cp-name"))
                                .append("videoReference", "ref")
                        )
                        .append(
                            "playback", Document()
                                .append("id", "some-id")
                                .append("type", "KALTURA")
                                .append("thumbnailUrl", listOf("thumbnail"))
                                .append("downloadUrl", "x")
                                .append("dashStreamUrl", "x")
                                .append("hlsStreamUrl", "x")
                                .append("progressiveStreamUrl", "x")
                                .append("duration", 10)
                        )
                        .append("legacy", Document().append("type", LegacyVideoType.NEWS.name))
                        .append("keywords", emptyList<String>())
                        .append("subjects", emptyList<String>())
                        .append("releaseDate", Date())
                        .append("durationSeconds", 10)
                        .append("legalRestrictions", "Some restrictions")
                        .append("language", "en")
                        .append("searchable", true)
                )

            val video = mongoVideoRepository.find(VideoId(value = "5c55697860fef77aa4af323a"))!!

            assertThat(video.isPlayable()).isTrue()
            assertThat(video.title).isEqualTo("Mah Video")
            assertThat(video.description).isEqualTo("Ain't no video like this one")
            assertThat(video.playback.id.value).isEqualTo("some-id")
            assertThat(video.playback.id.type.name).isEqualTo("KALTURA")
        }
    }
}

package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.LegacySubject
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.asset.Topic
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetFilter
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.Date
import java.util.Locale

class MongoVideoAssetRepositoryIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mongoVideoRepository: MongoVideoAssetRepository

    @Test
    fun `create a video`() {
        val asset = TestFactories.createVideoAsset()
        val createdAsset = mongoVideoRepository.create(asset)

        assertThat(createdAsset).isEqualTo(asset)
    }

    @Test
    fun `find a video`() {
        val originalAsset = mongoVideoRepository.create(TestFactories.createVideoAsset())
        val retrievedAsset = mongoVideoRepository.find(originalAsset.assetId)!!

        assertThat(retrievedAsset).isEqualTo(originalAsset)
    }

    @Test
    fun `find video when does not exist`() {
        assertThat(mongoVideoRepository.find(AssetId(TestFactories.aValidId()))).isNull()
    }

    @Test
    fun `lookup videos by ids maintains video order`() {
        val id1 = mongoVideoRepository.create(TestFactories.createVideoAsset()).assetId
        val id2 = mongoVideoRepository.create(TestFactories.createVideoAsset()).assetId
        val id3 = mongoVideoRepository.create(TestFactories.createVideoAsset()).assetId

        val videos = mongoVideoRepository.findAll(listOf(id2, id1, id3))

        assertThat(videos.map { it.assetId }).containsExactly(id2, id1, id3)
    }

    @Test
    fun `delete a video`() {
        val id = TestFactories.aValidId()
        val originalAsset = TestFactories.createVideoAsset(videoId = id)

        mongoVideoRepository.create(originalAsset)
        mongoVideoRepository.delete(AssetId(id))

        assertThat(mongoVideoRepository.find(AssetId(id))).isNull()
    }

    @Test
    fun `stream all searchable videos`() {
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = TestFactories.aValidId()))
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = TestFactories.aValidId()))
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = TestFactories.aValidId()))
        mongoVideoRepository.create(
            TestFactories.createVideoAsset(
                videoId = TestFactories.aValidId(),
                searchable = false
            )
        )

        var videos: List<VideoAsset> = emptyList()
        mongoVideoRepository.streamAll(VideoAssetFilter.IsSearchable) { videos = it.toList() }

        assertThat(videos).hasSize(3)
    }

    @Test
    fun `stream all by content partner`() {
        mongoVideoRepository.create(TestFactories.createVideoAsset(contentPartnerId = "TED"))
        mongoVideoRepository.create(TestFactories.createVideoAsset(contentPartnerId = "Bob"))
        mongoVideoRepository.create(TestFactories.createVideoAsset(contentPartnerId = "TED"))

        var videos: List<VideoAsset> = emptyList()
        mongoVideoRepository.streamAll(VideoAssetFilter.ContentPartnerIs("TED")) { videos = it.toList() }

        assertThat(videos).hasSize(2)
    }

    @Test
    @Disabled
    fun `can update playback`() {
        val originalAsset = mongoVideoRepository.create(
            TestFactories.createVideoAsset(
                playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1"),
                playback = null
            )
        )

        mongoVideoRepository.bulkUpdate(
            listOf(
                VideoUpdateCommand.ReplacePlayback(
                    originalAsset.assetId,
                    TestFactories.createKalturaPlayback(
                        downloadUrl = "download-url",
                        playbackId = "ref-123",
                        duration = Duration.ZERO,
                        thumbnailurl = "thumbnailUrl-url",
                        streamUrl = "stream-url"
                    )
                )
            )
        )

        val updatedAsset = mongoVideoRepository.find(originalAsset.assetId)

        assertThat(updatedAsset!!.playbackId).isNotNull
        assertThat(updatedAsset.playback!!.id).isEqualTo("ref-123")
        assertThat(updatedAsset.playback!!.thumbnailUrl).isEqualTo("thumnbnail-url")
        assertThat(updatedAsset.playback!!.duration).isEqualTo(Duration.ZERO)
        assertThat((updatedAsset.playback!! as StreamPlayback).downloadUrl).isEqualTo("download-url")
        assertThat((updatedAsset.playback!! as StreamPlayback).appleHlsStreamUrl).isEqualTo("stream-url")
    }

    @Test
    fun `can update subjects`() {
        val originalAsset = mongoVideoRepository.create(
            TestFactories.createVideoAsset(
                title = "original title",
                subjects = setOf(LegacySubject("Maths"))
            )
        )

        val updatedAsset = mongoVideoRepository.update(
            VideoUpdateCommand.ReplaceSubjects(
                originalAsset.assetId,
                listOf(LegacySubject("Biology"))
            )
        )

        assertThat(updatedAsset).isEqualToIgnoringGivenFields(originalAsset, "subjects")
        assertThat(updatedAsset.subjects).containsOnly(LegacySubject("Biology"))
    }

    @Test
    fun `update throws when video not found`() {
        assertThrows<VideoAssetNotFoundException> {
            mongoVideoRepository.update(
                VideoUpdateCommand.ReplaceDuration(
                    AssetId(value = TestFactories.aValidId()),
                    duration = Duration.ZERO
                )
            )
        }
    }

    @Test
    fun `bulk update applies multiple independent updates at once`() {
        val originalAsset1 = mongoVideoRepository.create(
            TestFactories.createVideoAsset(
                title = "original title 1",
                duration = Duration.ofMinutes(1),
                subjects = setOf(LegacySubject("German"))
            )
        )

        val originalAsset2 = mongoVideoRepository.create(
            TestFactories.createVideoAsset(
                title = "original title 2",
                duration = Duration.ofMinutes(99),
                subjects = setOf(LegacySubject("Maths"))
            )
        )

        val updates = listOf(
            VideoUpdateCommand.ReplaceSubjects(
                assetId = originalAsset1.assetId,
                subjects = emptyList()
            ),
            VideoUpdateCommand.ReplaceDuration(
                assetId = originalAsset1.assetId,
                duration = Duration.ofMinutes(10)
            ),
            VideoUpdateCommand.ReplaceSubjects(
                assetId = originalAsset2.assetId,
                subjects = listOf(LegacySubject("French"))
            ),
            VideoUpdateCommand.ReplaceDuration(
                assetId = originalAsset2.assetId,
                duration = Duration.ofMinutes(11)
            )
        )

        mongoVideoRepository.bulkUpdate(updates)

        val updatedAsset1 = mongoVideoRepository.find(originalAsset1.assetId)!!
        val updatedAsset2 = mongoVideoRepository.find(originalAsset2.assetId)!!

        assertThat(updatedAsset1).isEqualToIgnoringGivenFields(originalAsset1, "subjects", "duration")
        assertThat(updatedAsset1.duration).isEqualTo(Duration.ofMinutes(10))
        assertThat(updatedAsset1.subjects).isEmpty()

        assertThat(updatedAsset2).isEqualToIgnoringGivenFields(originalAsset2, "subjects", "duration")
        assertThat(updatedAsset2.duration).isEqualTo(Duration.ofMinutes(11))
        assertThat(updatedAsset2.subjects).isEqualTo(setOf(LegacySubject("French")))
    }

    @Test
    fun `find by content partner and content partner video id`() {
        val asset = TestFactories.createVideoAsset(
            videoId = TestFactories.aValidId(),
            contentPartnerVideoId = "ted-id-1",
            contentPartnerId = "TED Talks"
        )

        mongoVideoRepository.create(asset)

        assertThat(mongoVideoRepository.existsVideoFromContentPartner("TED Talks", "ted-id-1")).isTrue()
        assertThat(mongoVideoRepository.existsVideoFromContentPartner("TED Talks", "ted-id-2")).isFalse()
        assertThat(mongoVideoRepository.existsVideoFromContentPartner("TED Talks abc", "ted-id-1")).isFalse()
    }

    @Test
    fun `updates searchable`() {
        val asset = mongoVideoRepository.create(TestFactories.createVideoAsset(searchable = true))

        mongoVideoRepository.update(VideoUpdateCommand.HideFromSearch(asset.assetId))

        assertThat(mongoVideoRepository.find(asset.assetId)!!.searchable).isEqualTo(false)

        mongoVideoRepository.update(VideoUpdateCommand.MakeSearchable(asset.assetId))

        assertThat(mongoVideoRepository.find(asset.assetId)!!.searchable).isEqualTo(true)
    }

    @Test
    fun `replaces language`() {
        val asset = mongoVideoRepository.create(TestFactories.createVideoAsset(language = Locale.TAIWAN))

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceLanguage(asset.assetId, Locale.GERMAN))

        val updatedAsset = mongoVideoRepository.find(asset.assetId)

        assertThat(updatedAsset!!.language).isEqualTo(Locale.GERMAN)
    }

    @Test
    fun `replaces transcript`() {
        val asset = mongoVideoRepository.create(TestFactories.createVideoAsset(transcript = null))

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceTranscript(asset.assetId, "bla bla bla"))

        val updatedAsset = mongoVideoRepository.find(asset.assetId)

        assertThat(updatedAsset!!.transcript).isEqualTo("bla bla bla")
    }

    @Test
    fun `replaces topics`() {
        val asset = mongoVideoRepository.create(TestFactories.createVideoAsset(transcript = null))
        val topic = Topic(
            name = "Bayesian Methods",
            language = Locale.US,
            confidence = 0.85,
            parent = Topic(name = "Statistics", confidence = 1.0, language = Locale.US, parent = null)
        )

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceTopics(asset.assetId, setOf(topic)))

        val updatedAsset = mongoVideoRepository.find(asset.assetId)

        assertThat(updatedAsset!!.topics).containsExactly(topic)
    }

    @Test
    fun `replaces keywords`() {
        val asset = mongoVideoRepository.create(TestFactories.createVideoAsset(keywords = listOf("old")))

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceKeywords(asset.assetId, setOf("new")))

        val updatedAsset = mongoVideoRepository.find(asset.assetId)

        assertThat(updatedAsset!!.keywords).containsExactly("new")
    }

    @Nested
    inner class MigrationTests {
        @Test
        fun `can retrieve legacy video documents`() {
            mongoClient
                .getDatabase(DATABASE_NAME)
                .getCollection(MongoVideoAssetRepository.collectionName)
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

            val video = mongoVideoRepository.find(AssetId(value = "5c55697860fef77aa4af323a"))!!

            assertThat(video.title).isEqualTo("Mah Video")
            assertThat(video.description).isEqualTo("Ain't no video like this one")
            assertThat(video.playbackId.value).isEqualTo("some-id")
            assertThat(video.playbackId.type.name).isEqualTo("KALTURA")
        }
    }
}

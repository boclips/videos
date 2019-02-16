package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.service.video.ReplaceDuration
import com.boclips.videos.service.domain.service.video.ReplaceSubjects
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.time.LocalDate

class MongoVideoAssetRepositoryIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mongoVideoRepository: MongoVideoAssetRepository

    @Test
    fun `create a video`() {
        val asset = mongoVideoRepository.create(TestFactories.createVideoAsset())

        assertThat(asset.assetId.value).hasSize(24)
    }

    @Test
    fun `find a video`() {
        val originalAsset = mongoVideoRepository.create(TestFactories.createVideoAsset())

        val retrievedAsset = mongoVideoRepository.find(originalAsset.assetId)!!

        assertThat(retrievedAsset).isEqualToComparingFieldByFieldRecursively(originalAsset)
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
        mongoVideoRepository.streamAllSearchable { videos = it.toList() }

        assertThat(videos).hasSize(3)
    }

    @Test
    fun `update can update all attributes except asset ID`() {
        val originalAsset = mongoVideoRepository.create(
            TestFactories.createVideoAsset(
                title = "title",
                description = "description",
                contentProvider = "AP",
                contentPartnerVideoId = "cp-id-123",
                playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1"),
                type = LegacyVideoType.INSTRUCTIONAL_CLIPS,
                keywords = listOf("keywords"),
                subjects = emptySet(),
                releasedOn = LocalDate.parse("2018-01-01"),
                duration = Duration.ZERO,
                legalRestrictions = "",
                searchable = true
            )
        )

        val updatedAsset = mongoVideoRepository.update(
            ReplaceDuration(
                originalAsset.assetId,
                Duration.ofMinutes(5)
            )
        )

        assertThat(updatedAsset).isEqualToIgnoringGivenFields(originalAsset, "assetId", "duration")
        assertThat(updatedAsset.duration).isEqualTo(Duration.ofMinutes(5))
    }

    @Test
    fun `update doesn't touch unspecified attributes`() {
        val originalAsset = mongoVideoRepository.create(
            TestFactories.createVideoAsset(
                title = "original title",
                subjects = setOf(Subject("Maths"))
            )
        )

        val updatedAsset = mongoVideoRepository.update(
            ReplaceSubjects(
                originalAsset.assetId,
                listOf()
            )
        )

        assertThat(updatedAsset.subjects).isEmpty()
        assertThat(updatedAsset.title).isEqualTo("original title")
    }

    @Test
    fun `update throws when video not found`() {
        assertThrows<VideoAssetNotFoundException> {
            mongoVideoRepository.update(
                ReplaceDuration(
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
                subjects = setOf(Subject("German"))
            )
        )

        val originalAsset2 = mongoVideoRepository.create(
            TestFactories.createVideoAsset(
                title = "original title 2",
                duration = Duration.ofMinutes(99),
                subjects = setOf(Subject("Maths"))
            )
        )

        val updates = listOf(
            ReplaceSubjects(
                assetId = originalAsset1.assetId,
                subjects = emptyList()
            ),
            ReplaceDuration(
                assetId = originalAsset1.assetId,
                duration = Duration.ofMinutes(10)
            ),
            ReplaceSubjects(
                assetId = originalAsset2.assetId,
                subjects = listOf(Subject("French"))
            ),
            ReplaceDuration(
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
        assertThat(updatedAsset2.subjects).isEqualTo(setOf(Subject("French")))
    }

    @Test
    fun `find by content partner and content partner video id`() {
        val asset = TestFactories.createVideoAsset(
            videoId = TestFactories.aValidId(),
            contentPartnerVideoId = "ted-id-1",
            contentProvider = "TED Talks"
        )

        mongoVideoRepository.create(asset)

        assertThat(mongoVideoRepository.existsVideoFromContentPartner("TED Talks", "ted-id-1")).isTrue()
        assertThat(mongoVideoRepository.existsVideoFromContentPartner("TED Talks", "ted-id-2")).isFalse()
        assertThat(mongoVideoRepository.existsVideoFromContentPartner("TED Talks abc", "ted-id-1")).isFalse()
    }

    @Test
    fun `mark videos as searchable`() {
        val videoAsset1 = TestFactories.createVideoAsset()
        mongoVideoRepository.create(videoAsset1)

        val videoAsset2 = TestFactories.createVideoAsset()
        mongoVideoRepository.create(videoAsset2)

        val videoAssets = listOf(videoAsset1.assetId, videoAsset2.assetId)

        mongoVideoRepository.makeSearchable(videoAssets)
        val updatedVideoAssets = mongoVideoRepository.findAll(videoAssets)

        assertThat(updatedVideoAssets.map { it.searchable }).containsExactly(true, true)
    }

    @Test
    fun `mark videos disabled for search`() {
        val videoAsset1 = TestFactories.createVideoAsset()
        mongoVideoRepository.create(videoAsset1)

        val videoAsset2 = TestFactories.createVideoAsset()
        mongoVideoRepository.create(videoAsset2)

        val videoAssets = listOf(videoAsset1.assetId, videoAsset2.assetId)

        mongoVideoRepository.disableFromSearch(videoAssets)
        val updatedVideoAssets = mongoVideoRepository.findAll(videoAssets)

        assertThat(updatedVideoAssets.map { it.searchable }).containsExactly(false, false)
    }
}
package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.asset.PartialVideoAsset
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
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
        val originalAsset = mongoVideoRepository.create(TestFactories.createVideoAsset(
            title = "title",
            description = "description",
            contentProvider= "AP",
            contentPartnerVideoId = "cp-id-123",
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1"),
            type = LegacyVideoType.INSTRUCTIONAL_CLIPS,
            keywords = listOf("keywords"),
            subjects = emptySet(),
            releasedOn = LocalDate.parse("2018-01-01"),
            duration = Duration.ZERO,
            legalRestrictions = "",
            searchable = true
        ))

        val requestedUpdates = PartialVideoAsset(
            title = "title updated",
            description = "description updated",
            contentPartnerId = "AP updated",
            contentPartnerVideoId = "cp-id-123-updated",
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "ref-id-1-updated"),
            type = LegacyVideoType.STOCK,
            keywords = listOf("keywords", "updated"),
            subjects = setOf(Subject(name = "updated")),
            releasedOn = LocalDate.parse("2019-12-12"),
            duration = Duration.ofMinutes(2),
            legalRestrictions = "Too many",
            searchable = false
        )

        val updatedAsset = mongoVideoRepository.update(originalAsset.assetId, requestedUpdates)

        assertThat(updatedAsset).isEqualToIgnoringGivenFields(requestedUpdates, "assetId")
    }

    @Test
    fun `update doesn't touch unspecified attributes`() {
        val originalAsset = mongoVideoRepository.create(TestFactories.createVideoAsset(
            title = "original title",
            description = "original description"
        ))

        val requestedUpdates = PartialVideoAsset(
            description = "new description"
        )

        val updatedAsset = mongoVideoRepository.update(originalAsset.assetId, requestedUpdates)

        assertThat(updatedAsset.description).isEqualTo("new description")
        assertThat(updatedAsset.title).isEqualTo("original title")
    }

    @Test
    fun `update throws when video not found`() {
        val asset = TestFactories.createVideoAsset(videoId = TestFactories.aValidId())

        assertThrows<VideoAssetNotFoundException> { mongoVideoRepository.update(
            assetId = asset.assetId,
            attributes = PartialVideoAsset(title = "title")
        ) }
    }

    @Test
    fun `bulk update applies multiple independent updates at once`() {
        val originalAsset1 = mongoVideoRepository.create(TestFactories.createVideoAsset(
            title = "original title 1",
            description = "original description 1"
        ))

        val originalAsset2 = mongoVideoRepository.create(TestFactories.createVideoAsset(
            title = "original title 2",
            description = "original description 2"
        ))

        val updates = listOf(
            Pair(originalAsset1.assetId, PartialVideoAsset(title = "New title 1")),
            Pair(originalAsset2.assetId, PartialVideoAsset(description = "New description 2"))
        )

        mongoVideoRepository.bulkUpdate(updates)

        val updatedAsset1 = mongoVideoRepository.find(originalAsset1.assetId)!!
        val updatedAsset2 = mongoVideoRepository.find(originalAsset2.assetId)!!

        assertThat(updatedAsset1).isEqualToIgnoringGivenFields(originalAsset1, "title")
        assertThat(updatedAsset1.title).isEqualTo("New title 1")

        assertThat(updatedAsset2).isEqualToIgnoringGivenFields(originalAsset2, "description")
        assertThat(updatedAsset2.description).isEqualTo("New description 2")
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
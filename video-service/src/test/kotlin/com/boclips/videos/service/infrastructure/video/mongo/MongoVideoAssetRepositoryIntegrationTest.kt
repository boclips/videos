package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

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
    fun `stream all videos`() {
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = TestFactories.aValidId()))
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = TestFactories.aValidId()))
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = TestFactories.aValidId()))

        var videos: List<VideoAsset> = emptyList()
        mongoVideoRepository.streamAll { videos = it.toList() }

        assertThat(videos).hasSize(3)
    }

    @Test
    fun `update a video`() {
        val existingAsset = mongoVideoRepository.create(
                TestFactories.createVideoAsset(
                        title = "old title",
                        keywords = listOf("k1", "k2"),
                        playbackId = PlaybackId(PlaybackProviderType.KALTURA, "old-id")
                )
        )

        val assetToBeUpdated = existingAsset.copy(
                title = "new title",
                keywords = listOf("k2"),
                playbackId = PlaybackId(PlaybackProviderType.YOUTUBE, "new-id")

        )
        val updatedAsset = mongoVideoRepository.update(assetToBeUpdated)

        assertThat(updatedAsset).isEqualTo(assetToBeUpdated)
        assertThat(mongoVideoRepository.find(existingAsset.assetId)).isEqualTo(assetToBeUpdated)
    }

    @Test
    fun `update throws when video not found`() {
        val asset = TestFactories.createVideoAsset(
                videoId = TestFactories.aValidId()
        )

        assertThrows<VideoAssetNotFoundException> { mongoVideoRepository.update(asset) }
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
}
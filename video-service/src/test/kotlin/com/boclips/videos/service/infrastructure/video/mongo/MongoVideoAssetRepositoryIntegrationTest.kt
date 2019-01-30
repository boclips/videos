package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class MongoVideoAssetRepositoryIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mongoVideoRepository: MongoVideoAssetRepository

    @Test
    fun `create and find a video`() {
        val id = ObjectId().toHexString()
        val originalAsset = TestFactories.createVideoAsset(videoId = id)

        mongoVideoRepository.create(originalAsset)
        val retrievedAsset = mongoVideoRepository.find(AssetId(id))!!

        assertThat(retrievedAsset).isEqualToComparingFieldByFieldRecursively(originalAsset)
    }

    @Test
    fun `find video when does not exist`() {
        assertThat(mongoVideoRepository.find(AssetId(ObjectId().toHexString()))).isNull()
    }

    @Test
    fun `lookup videos by ids maintains video order`() {
        val id1 = ObjectId().toHexString()
        val id2 = ObjectId().toHexString()
        val id3 = ObjectId().toHexString()

        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = id1))
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = id2))
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = id3))

        val videos = mongoVideoRepository.findAll(listOf(
                AssetId(id2),
                AssetId(id1),
                AssetId(id3)
        ))

        assertThat(videos.map { it.assetId.value }).containsExactly(id2, id1, id3)
    }

    @Test
    fun `delete a video`() {
        val id = ObjectId().toHexString()
        val originalAsset = TestFactories.createVideoAsset(videoId = id)

        mongoVideoRepository.create(originalAsset)
        mongoVideoRepository.delete(AssetId(id))

        assertThat(mongoVideoRepository.find(AssetId(id))).isNull()
    }

    @Test
    fun `stream all videos`() {
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = ObjectId().toHexString()))
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = ObjectId().toHexString()))
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = ObjectId().toHexString()))

        var videos: List<VideoAsset> = emptyList()
        mongoVideoRepository.streamAll { videos = it.toList() }

        assertThat(videos).hasSize(3)
    }

    @Test
    fun `update a video`() {
        val id = ObjectId().toHexString()
        val existingAsset = TestFactories.createVideoAsset(
                videoId = id,
                title = "old title",
                keywords = listOf("k1", "k2"),
                playbackId = PlaybackId(PlaybackProviderType.KALTURA, "old-id")
        )

        mongoVideoRepository.create(existingAsset)

        val assetToBeUpdated = existingAsset.copy(
                title = "new title",
                keywords = listOf("k2"),
                playbackId = PlaybackId(PlaybackProviderType.YOUTUBE, "new-id")

        )
        val updatedAsset = mongoVideoRepository.update(assetToBeUpdated)

        assertThat(updatedAsset).isEqualTo(assetToBeUpdated)
        assertThat(mongoVideoRepository.find(AssetId(id))).isEqualTo(assetToBeUpdated)
    }

    @Test
    fun `update throws when video not found`() {
        val asset = TestFactories.createVideoAsset(
                videoId = ObjectId().toHexString()
        )

        assertThrows<VideoAssetNotFoundException> { mongoVideoRepository.update(asset) }
    }

    @Test
    fun `find by content partner and content partner video id`() {
        val asset = TestFactories.createVideoAsset(
                videoId = ObjectId().toHexString(),
                contentPartnerVideoId = "ted-id-1",
                contentProvider = "TED Talks"
        )

        mongoVideoRepository.create(asset)

        assertThat(mongoVideoRepository.existsVideoFromContentPartner("TED Talks", "ted-id-1")).isTrue()
        assertThat(mongoVideoRepository.existsVideoFromContentPartner("TED Talks", "ted-id-2")).isFalse()
        assertThat(mongoVideoRepository.existsVideoFromContentPartner("TED Talks abc", "ted-id-1")).isFalse()
    }
}
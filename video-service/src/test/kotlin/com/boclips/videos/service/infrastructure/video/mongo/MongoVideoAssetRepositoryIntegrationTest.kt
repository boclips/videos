package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.service.VideoSubjectsUpdate
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
    fun `stream all searchable videos`() {
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = TestFactories.aValidId()))
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = TestFactories.aValidId()))
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = TestFactories.aValidId()))
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = TestFactories.aValidId(), searchable = false))

        var videos: List<VideoAsset> = emptyList()
        mongoVideoRepository.streamAllSearchable { videos = it.toList() }

        assertThat(videos).hasSize(3)
    }

    @Test
    fun `update video subjects`() {
        val existingAsset = mongoVideoRepository.create(
                TestFactories.createVideoAsset(
                        title = "old title",
                        keywords = listOf("k1", "k2"),
                        playbackId = PlaybackId(PlaybackProviderType.KALTURA, "old-id"),
                        subjects = setOf(Subject("physics"))
                )
        )

        mongoVideoRepository.replaceSubjects(existingAsset.assetId, listOf(Subject("maths")))

        assertThat(mongoVideoRepository.find(existingAsset.assetId)).isEqualTo(existingAsset.copy(subjects = setOf(Subject("maths"))))
    }

    @Test
    fun `update throws when video not found`() {
        val asset = TestFactories.createVideoAsset(
                videoId = TestFactories.aValidId()
        )

        assertThrows<VideoAssetNotFoundException> { mongoVideoRepository.replaceSubjects(asset.assetId, emptyList()) }
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
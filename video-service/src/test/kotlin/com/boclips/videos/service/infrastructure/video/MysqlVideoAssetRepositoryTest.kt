package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MysqlVideoAssetRepositoryTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRepository: MysqlVideoAssetRepository

    @Test
    fun `findVideosBy can find multiple videos by video ids`() {
        saveVideo(videoId = 123, title = "Some title", description = "test description 3")
        saveVideo(videoId = 124, title = "Some title", description = "test description 3")
        saveVideo(videoId = 125, title = "Some title", description = "test description 3")

        val videos = videoRepository.findAll(listOf(AssetId(value = "123"), AssetId(value = "124"), AssetId(value = "125")))

        assertThat(videos).hasSize(3)
    }

    @Test
    fun `findVideosBy does not throw when one video can't be found`() {
        saveVideo(videoId = 123, title = "Some title", description = "test description 3")
        saveVideo(videoId = 124, title = "Some title", description = "test description 3")

        val videos = videoRepository.findAll(listOf(AssetId(value = "123"), AssetId(value = "124"), AssetId(value = "125")))

        assertThat(videos).hasSize(2)
    }

    @Test
    fun `findVideoBy returns video details`() {
        saveVideo(videoId = 123, title = "Some title", description = "test description 3")

        val videoId = AssetId(value = "123")
        val video = videoRepository.find(videoId)!!

        assertThat(video.assetId.value).isEqualTo("123")
        assertThat(video.playbackId.value).isNotNull()
        assertThat(video.playbackId.type).isNotNull()
        assertThat(video.description).isNotEmpty()
        assertThat(video.title).isNotEmpty()
        assertThat(video.contentPartnerId).isNotEmpty()
        assertThat(video.releasedOn).isNotNull()
    }

    @Test
    fun `findVideoBy returns null when video does not exist`() {
        assertThat(videoRepository.find(AssetId(value = "999"))).isNull()
    }

    @Test
    fun `video cannot be retrieved after it has been removed`() {
        val videoId = AssetId("123")
        saveVideo(videoId = videoId.value.toLong(), title = "Some title", description = "test description 3")

        videoRepository.delete(videoId)

        assertThat(videoRepository.findAll(listOf(videoId))).isEmpty()
    }

    @Test
    fun `createVideo inserts a video`() {
        val videoAsset = videoRepository.create(TestFactories.createVideoAsset(videoId = ""))
        assertThat(videoAsset.assetId.value).isNotBlank()
        assertThat(videoRepository.findAll(listOf(videoAsset.assetId))).isNotEmpty
    }

    @Test
    fun `findByContentPartner checks both partner id and partner video id`() {
        saveVideo(videoId = 123, contentProvider = "ted", contentProviderId = "abc")

        assertThat(videoRepository.existsVideoFromContentPartner("ted", "abc")).isTrue()
        assertThat(videoRepository.existsVideoFromContentPartner("teddy", "abc")).isFalse()
        assertThat(videoRepository.existsVideoFromContentPartner("ted", "abcd")).isFalse()
    }
}

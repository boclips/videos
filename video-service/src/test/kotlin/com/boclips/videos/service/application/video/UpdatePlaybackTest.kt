package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import com.mongodb.MongoClientException
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class UpdatePlaybackTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var playbackRepository: PlaybackRepository

    @Autowired
    lateinit var videoAssetRepository: VideoAssetRepository

    @Autowired
    lateinit var updatePlayback: UpdatePlayback

    @Test
    @Disabled
    fun `updates playback information of video asset`() {
        fakeKalturaClient.addMediaEntry(
            createMediaEntry(
                referenceId = "ref-id-100",
                duration = Duration.ZERO
            )
        )

        val existingAsset = videoAssetRepository.create(
            TestFactories.createVideoAsset(
                playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-100"),
                playback = null
            )
        )

        updatePlayback.invoke()

        val updatedAsset = videoAssetRepository.find(existingAsset.assetId)
        assertThat(updatedAsset!!.playback!!.duration).isEqualTo(Duration.ZERO)
        assertThat(updatedAsset.playback!!.thumbnailUrl).isEqualTo("x")
        assertThat(updatedAsset.playback!!.id).isEqualTo("ref-id-100")
        assertThat((updatedAsset.playback!! as StreamPlayback).downloadUrl).isNotEmpty()
        assertThat((updatedAsset.playback!! as StreamPlayback).streamUrl).isNotEmpty()
    }

    @Test
    fun `the future surfaces any underlying exceptions`() {
        val videoAssetRepository = mock<VideoAssetRepository> {
            on {
                streamAll(any(), any())
            } doThrow (MongoClientException("Boom"))
        }

        val refreshVideoPlaybacks = UpdatePlayback(videoAssetRepository, playbackRepository)

        assertThat(refreshVideoPlaybacks()).hasFailedWithThrowableThat().hasMessage("Boom")
    }
}

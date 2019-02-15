package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import com.boclips.videos.service.testsupport.TestFactories.createVideoAsset
import com.mongodb.MongoClientException
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class RefreshVideoDurationsTest: AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var playbackRepository: PlaybackRepository

    @Autowired
    lateinit var videoAssetRepository: VideoAssetRepository

    @Test
    fun `updates durations with the playback provider's value`() {
        val refreshVideoDurations = RefreshVideoDurations(videoAssetRepository, playbackRepository)

        val incorrectDuration = Duration.ZERO
        val playbackDuration1 = Duration.ofMinutes(1)
        val playbackDuration2 = Duration.ofMinutes(2)

        val playbackId1 = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-100")
        val playbackId2 = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-202")

        val asset1 = videoAssetRepository.create(
            createVideoAsset(
                title = "Wrong duration",
                playbackId = playbackId1,
                duration = incorrectDuration
            )
        )
        val asset2 = videoAssetRepository.create(
            createVideoAsset(
                title = "Wrong duration II",
                playbackId = playbackId2,
                duration = incorrectDuration
            )
        )

        fakeKalturaClient.addMediaEntry(createMediaEntry(referenceId = playbackId1.value, duration = playbackDuration1))
        fakeKalturaClient.addMediaEntry(createMediaEntry(referenceId = playbackId2.value, duration = playbackDuration2))

        refreshVideoDurations.invoke()

        assertThat(videoAssetRepository.find(asset1.assetId)!!.duration).isEqualTo(playbackDuration1)
        assertThat(videoAssetRepository.find(asset2.assetId)!!.duration).isEqualTo(playbackDuration2)
    }

    @Test
    fun `does not update the asset duration when it matches the playback duration`() {
        val playbackDuration = Duration.ofMinutes(1)
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-100")

        val asset = createVideoAsset(
            title = "Wrong duration",
            playbackId = playbackId,
            duration = playbackDuration
        )

        fakeKalturaClient.addMediaEntry(createMediaEntry(referenceId = playbackId.value, duration = playbackDuration))

        val mockVideoAssetRepository = mock<VideoAssetRepository> {
            on {
                streamAllSearchable(any())
            } doAnswer { invocations ->
                val consumer = invocations.getArgument(0) as (Sequence<VideoAsset>) -> Unit
                consumer(sequenceOf(asset))
            }
        }

        val refreshVideoDurations = RefreshVideoDurations(mockVideoAssetRepository, playbackRepository)

        refreshVideoDurations.invoke()

        verify(mockVideoAssetRepository, never()).update(any(), any())
        verify(mockVideoAssetRepository, never()).bulkUpdate(any())
    }

    @Test
    fun `the future surfaces any underlying exceptions`() {
        val videoAssetRepository = mock<VideoAssetRepository> {
            on {
                streamAllSearchable(any())
            } doThrow(MongoClientException("Boom"))
        }

        val refreshVideoDurations = RefreshVideoDurations(videoAssetRepository, playbackRepository)

        assertThat(refreshVideoDurations()).hasFailedWithThrowableThat().hasMessage("Boom")
    }
}
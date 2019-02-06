package com.boclips.videos.service.infrastructure.playback

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class KalturaPlaybackProviderTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var kalturaClient: TestKalturaClient

    @Test
    fun `returns streaming information for videos`() {
        fakeKalturaClient.addMediaEntry(createMediaEntry("1"))

        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1")
        val playbackById = kalturaPlaybackProvider.retrievePlayback(listOf(playbackId))

        assertThat(playbackById).hasSize(1)
        assertThat(playbackById[playbackId]).isNotNull

        val videoPlayback = playbackById[playbackId] as StreamPlayback
        assertThat(videoPlayback.streamUrl).isEqualTo("https://stream/applehttp/asset-1.mp4")
        assertThat(videoPlayback.thumbnailUrl).isEqualTo("https://thumbnail/thumbnail-1.mp4")
        assertThat(videoPlayback.duration).isEqualTo(Duration.parse("PT1M"))
    }

    @Test
    fun `returns only videos with streaming information, omits the others`() {
        fakeKalturaClient.addMediaEntry(createMediaEntry("1"))
        val existingPlaybackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1")
        val inexistantPlaybackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-100")

        val videosWithPlayback = kalturaPlaybackProvider.retrievePlayback(
                listOf(existingPlaybackId, inexistantPlaybackId)
        )

        assertThat(videosWithPlayback).hasSize(1)
        assertThat(videosWithPlayback[existingPlaybackId]).isNotNull
        assertThat(videosWithPlayback[inexistantPlaybackId]).isNull()
    }

    @Test
    fun `removes the playback information`() {
        kalturaClient.addMediaEntry(MediaEntry.builder()
                .id("something")
                .referenceId("ref-123")
                .build())

        val playbackToBeDeleted = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-123")
        kalturaPlaybackProvider.removePlayback(playbackToBeDeleted)

        assertThat(kalturaPlaybackProvider.retrievePlayback(listOf(playbackToBeDeleted))).isEmpty()
    }

    @Test
    fun `filters non ready kaltura videos`() {
        fakeKalturaClient.addMediaEntry(createMediaEntry("1", status = MediaEntryStatus.NOT_READY))
        fakeKalturaClient.addMediaEntry(createMediaEntry("2", status = MediaEntryStatus.READY))

        val playbackIdOfNonReady = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1")
        val playbackIdOfReady = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-2")

        val videosWithPlayback = kalturaPlaybackProvider.retrievePlayback(
                listOf(playbackIdOfNonReady, playbackIdOfReady)
        )

        assertThat(videosWithPlayback).hasSize(1)
        assertThat(videosWithPlayback[playbackIdOfReady]).isNotNull
        assertThat(videosWithPlayback[playbackIdOfNonReady]).isNull()

    }

}

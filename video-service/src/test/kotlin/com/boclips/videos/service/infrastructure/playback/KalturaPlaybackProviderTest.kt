package com.boclips.videos.service.infrastructure.playback

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.kalturaclient.captionasset.CaptionFormat
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.createCaptions
import com.boclips.videos.service.testsupport.TestFactories.createKalturaCaptionAsset
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
        assertThat(videoPlayback.downloadUrl).isEqualTo("https://download/video-1.mp4")
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
    fun `uploads captions if there are no captions in Kaltura already`() {
        val existingArabicCaptions = createKalturaCaptionAsset(language = "Arabic")
        fakeKalturaClient.addMediaEntry(createMediaEntry(referenceId = "ref-id"))
        fakeKalturaClient.createCaptionsFile("ref-id", existingArabicCaptions, "bla bla in arabic")
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id")

        val newEnglishCaptions = createCaptions(language = "en-UK", content = "bla bla bla in english")
        kalturaPlaybackProvider.uploadCaptions(playbackId, newEnglishCaptions)

        val allCaptions = fakeKalturaClient.getCaptionFilesByReferenceId("ref-id")
        assertThat(allCaptions).hasSize(2)
        val englishCaptions = allCaptions.find { it.language != "Arabic" }!!
        assertThat(englishCaptions.language).isEqualTo("English")
        assertThat(englishCaptions.label).isEqualTo("English (auto-generated)")
        assertThat(englishCaptions.fileType).isEqualTo(CaptionFormat.WEBVTT)
        assertThat(fakeKalturaClient.getCaptionContentByAssetId(englishCaptions.id)).isEqualTo("bla bla bla in english")
    }

    @Test
    fun `does not upload captions if there are captions in Kaltura already`() {
        val existingCaptions = createKalturaCaptionAsset(language = "English")
        fakeKalturaClient.addMediaEntry(createMediaEntry(referenceId = "ref-id"))
        fakeKalturaClient.createCaptionsFile("ref-id", existingCaptions, "bla")
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id")

        val newCaptions = createCaptions(language = "en-UK")
        kalturaPlaybackProvider.uploadCaptions(playbackId, newCaptions)

        val allCaptions = fakeKalturaClient.getCaptionFilesByReferenceId("ref-id")
        assertThat(allCaptions).hasSize(1)
    }

    @Test
    fun `removes the playback information`() {
        kalturaClient.addMediaEntry(
            MediaEntry.builder()
                .id("something")
                .referenceId("ref-123")
                .build()
        )

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

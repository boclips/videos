package com.boclips.videos.service.infrastructure.playback

import com.boclips.kalturaclient.captionasset.CaptionFormat
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.createCaptions
import com.boclips.videos.service.testsupport.TestFactories.createKalturaCaptionAsset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.Locale

class KalturaPlaybackProviderTest : AbstractSpringIntegrationTest() {

    @Test
    fun `returns streaming information for videos`() {
        createMediaEntry(id = "1")

        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "1")
        val playbackById = kalturaPlaybackProvider.retrievePlayback(listOf(playbackId))

        assertThat(playbackById).hasSize(1)
        assertThat(playbackById[playbackId]).isNotNull

        val videoPlayback = playbackById[playbackId] as StreamPlayback
        assertThat(videoPlayback.appleHlsStreamUrl).isEqualTo("https://stream.com/entryId/1/format/applehttp")
        assertThat(videoPlayback.mpegDashStreamUrl).isEqualTo("https://stream.com/entryId/1/format/mpegdash")
        assertThat(videoPlayback.progressiveDownloadStreamUrl).isEqualTo("https://stream.com/entryId/1/format/url")
        assertThat(videoPlayback.downloadUrl).isEqualTo("https://download.com/entryId/1/format/download")
        assertThat(videoPlayback.thumbnailUrl).isEqualTo("https://thumbnail.com/entry_id/1/width/{thumbnailWidth}")
        assertThat(videoPlayback.duration).isEqualTo(Duration.parse("PT1M"))
    }

    @Test
    fun `returns streaming information for videos based on reference id`() {
        createMediaEntry(id = "1", referenceId = "ref-id-1")

        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA_REFERENCE, value = "ref-id-1")
        val playbacksById = kalturaPlaybackProvider.retrievePlayback(listOf(playbackId))

        assertThat(playbacksById).hasSize(1)
        assertThat(playbacksById[playbackId]).isNotNull

        val videoPlayback = playbacksById[playbackId] as StreamPlayback
        assertThat(videoPlayback.id).isEqualTo(PlaybackId(type = PlaybackProviderType.KALTURA, value = "1"))
        assertThat(videoPlayback.appleHlsStreamUrl).isEqualTo("https://stream.com/entryId/1/format/applehttp")
        assertThat(videoPlayback.mpegDashStreamUrl).isEqualTo("https://stream.com/entryId/1/format/mpegdash")
        assertThat(videoPlayback.progressiveDownloadStreamUrl).isEqualTo("https://stream.com/entryId/1/format/url")
        assertThat(videoPlayback.downloadUrl).isEqualTo("https://download.com/entryId/1/format/download")
        assertThat(videoPlayback.thumbnailUrl).isEqualTo("https://thumbnail.com/entry_id/1/width/{thumbnailWidth}")
        assertThat(videoPlayback.duration).isEqualTo(Duration.parse("PT1M"))
    }

    @Test
    fun `returns several playbacks for videos based on reference and entry ids`() {
        createMediaEntry(id = "1", referenceId = "ref-id-1")
        createMediaEntry(id = "2", referenceId = "ref-id-2")
        createMediaEntry(id = "3", referenceId = "ref-id-3")
        createMediaEntry(id = "4", referenceId = "ref-id-4")

        val playbackIdOne = PlaybackId(type = PlaybackProviderType.KALTURA_REFERENCE, value = "ref-id-1")
        val playbackIdTwo = PlaybackId(type = PlaybackProviderType.KALTURA, value = "2")
        val playbackIdThree = PlaybackId(type = PlaybackProviderType.KALTURA, value = "3")
        val playbackIdFour = PlaybackId(type = PlaybackProviderType.KALTURA_REFERENCE, value = "ref-id-4")

        val playbacksById = kalturaPlaybackProvider.retrievePlayback(
            listOf(
                playbackIdOne, playbackIdTwo, playbackIdThree, playbackIdFour
            )
        )

        assertThat(playbacksById).hasSize(4)
        assertThat(playbacksById[playbackIdOne]).isNotNull
        assertThat(playbacksById[playbackIdTwo]).isNotNull
        assertThat(playbacksById[playbackIdThree]).isNotNull
        assertThat(playbacksById[playbackIdFour]).isNotNull
    }

    @Test
    fun `returns only videos with streaming information, omits the others`() {
        createMediaEntry(id = "1")
        val existingPlaybackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "1")
        val inexistantPlaybackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "100")

        val videosWithPlayback = kalturaPlaybackProvider.retrievePlayback(
            listOf(existingPlaybackId, inexistantPlaybackId)
        )

        assertThat(videosWithPlayback).hasSize(1)
        assertThat(videosWithPlayback[existingPlaybackId]).isNotNull
        assertThat(videosWithPlayback[inexistantPlaybackId]).isNull()
    }

    @Test
    fun `uploads captions if there are no captions in Kaltura already`() {
        val playbackId = mediaEntryWithCaptions(label = "Arabic", language = KalturaLanguage.ARABIC)

        val newEnglishCaptions = createCaptions(language = Locale.UK, content = "bla bla bla in english")
        kalturaPlaybackProvider.uploadCaptions(playbackId, newEnglishCaptions)

        val allCaptions = fakeKalturaClient.getCaptionFilesByReferenceId("ref-id")
        assertThat(allCaptions).hasSize(2)
        val englishCaptions = allCaptions.find { it.language != KalturaLanguage.ARABIC }!!
        assertThat(englishCaptions.language).isEqualTo(KalturaLanguage.ENGLISH)
        assertThat(englishCaptions.label).isEqualTo("English (auto-generated)")
        assertThat(englishCaptions.fileType).isEqualTo(CaptionFormat.WEBVTT)
        assertThat(fakeKalturaClient.getCaptionContentByAssetId(englishCaptions.id)).isEqualTo("bla bla bla in english")
    }

    @Test
    fun `uploads captions if there are no captions in Kaltura already by entry id`() {
        val playbackId = mediaEntryWithCaptionsByEntryId(label = "Arabic", language = KalturaLanguage.ARABIC)

        val newEnglishCaptions = createCaptions(language = Locale.UK, content = "bla bla bla in english")
        kalturaPlaybackProvider.uploadCaptions(playbackId, newEnglishCaptions)

        val allCaptions = fakeKalturaClient.getCaptionFilesByEntryId("entry-id")
        assertThat(allCaptions).hasSize(2)
        val englishCaptions = allCaptions.find { it.language != KalturaLanguage.ARABIC }!!
        assertThat(englishCaptions.language).isEqualTo(KalturaLanguage.ENGLISH)
        assertThat(englishCaptions.label).isEqualTo("English (auto-generated)")
        assertThat(englishCaptions.fileType).isEqualTo(CaptionFormat.WEBVTT)
        assertThat(fakeKalturaClient.getCaptionContentByAssetId(englishCaptions.id)).isEqualTo("bla bla bla in english")
    }

    @Test
    fun `does not upload captions if there are manually-created captions in Kaltura already`() {
        val playbackId = mediaEntryWithCaptions("English")

        val newCaptions = createCaptions(language = Locale.UK)
        kalturaPlaybackProvider.uploadCaptions(playbackId, newCaptions)

        val allCaptions = fakeKalturaClient.getCaptionFilesByReferenceId("ref-id")
        assertThat(allCaptions).hasSize(1)
        assertThat(allCaptions.first().label).isEqualTo("English")
    }

    @Test
    fun `does not upload captions if there are manually-created captions in Kaltura already by entry id`() {
        val playbackId = mediaEntryWithCaptionsByEntryId("English")

        val newCaptions = createCaptions(language = Locale.UK)
        kalturaPlaybackProvider.uploadCaptions(playbackId, newCaptions)

        val allCaptions = fakeKalturaClient.getCaptionFilesByEntryId("entry-id")
        assertThat(allCaptions).hasSize(1)
        assertThat(allCaptions.first().label).isEqualTo("English")
    }

    @Test
    fun `replaces captions if there are only automatically-created captions in Kaltura`() {
        val playbackId = mediaEntryWithCaptions("English (auto-generated)")

        kalturaPlaybackProvider.uploadCaptions(
            playbackId,
            createCaptions(language = Locale.UK, autoGenerated = true, content = "new captions")
        )

        val allCaptions = fakeKalturaClient.getCaptionFilesByReferenceId("ref-id")
        assertThat(allCaptions).hasSize(1)
        assertThat(fakeKalturaClient.getCaptionContentByAssetId(allCaptions.first().id)).isEqualTo("new captions")
    }

    @Test
    fun `replaces captions if there are only automatically-created captions in Kaltura by entry id`() {
        val playbackId = mediaEntryWithCaptionsByEntryId("English (auto-generated)")

        kalturaPlaybackProvider.uploadCaptions(
            playbackId,
            createCaptions(language = Locale.UK, autoGenerated = true, content = "new captions")
        )

        val allCaptions = fakeKalturaClient.getCaptionFilesByEntryId("entry-id")
        assertThat(allCaptions).hasSize(1)
        assertThat(fakeKalturaClient.getCaptionContentByAssetId(allCaptions.first().id)).isEqualTo("new captions")
    }

    @Test
    fun `deletes auto-generated captions when null`() {
        val playbackId = mediaEntryWithCaptions("English (auto-generated)")

        kalturaPlaybackProvider.deleteAutoGeneratedCaptions(playbackId, Locale.ENGLISH)

        val allCaptions = fakeKalturaClient.getCaptionFilesByReferenceId("ref-id")
        assertThat(allCaptions).isEmpty()
    }

    private fun mediaEntryWithCaptions(label: String, language: KalturaLanguage = KalturaLanguage.ENGLISH): PlaybackId {
        val existingCaptions = createKalturaCaptionAsset(label = label, language = language)
        createMediaEntry(referenceId = "ref-id")
        fakeKalturaClient.createCaptionsFile("ref-id", existingCaptions, "old captions")
        return PlaybackId(type = PlaybackProviderType.KALTURA_REFERENCE, value = "ref-id")
    }

    private fun mediaEntryWithCaptionsByEntryId(label: String, language: KalturaLanguage = KalturaLanguage.ENGLISH): PlaybackId {
        val existingCaptions = createKalturaCaptionAsset(label = label, language = language)
        createMediaEntry(id = "entry-id")
        fakeKalturaClient.createCaptionsFileWithEntryId("entry-id", existingCaptions, "old captions")
        return PlaybackId(type = PlaybackProviderType.KALTURA, value = "entry-id")
    }

    @Test
    fun `removes the playback information`() {
        createMediaEntry(id = "123")

        val playbackToBeDeleted = PlaybackId(type = PlaybackProviderType.KALTURA, value = "123")
        kalturaPlaybackProvider.removePlayback(playbackToBeDeleted)

        assertThat(kalturaPlaybackProvider.retrievePlayback(listOf(playbackToBeDeleted))).isEmpty()
    }

    @Test
    fun `filters non ready kaltura videos`() {
        createMediaEntry(id = "1", status = MediaEntryStatus.NOT_READY)
        createMediaEntry(id = "2", status = MediaEntryStatus.READY)

        val playbackIdOfNonReady = PlaybackId(type = PlaybackProviderType.KALTURA, value = "1")
        val playbackIdOfReady = PlaybackId(type = PlaybackProviderType.KALTURA, value = "2")

        val videosWithPlayback = kalturaPlaybackProvider.retrievePlayback(
            listOf(playbackIdOfNonReady, playbackIdOfReady)
        )

        assertThat(videosWithPlayback).hasSize(1)
        assertThat(videosWithPlayback[playbackIdOfReady]).isNotNull
        assertThat(videosWithPlayback[playbackIdOfNonReady]).isNull()
    }
}

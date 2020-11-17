package com.boclips.videos.service.infrastructure.playback

import com.boclips.kalturaclient.KalturaCaptionManager
import com.boclips.kalturaclient.captionasset.CaptionFormat
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.playback.Dimensions
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.CaptionFormat.WEBVTT
import com.boclips.videos.service.domain.model.playback.CaptionConflictException
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.KalturaFactories
import com.boclips.videos.service.testsupport.KalturaFactories.createKalturaCaptionAsset
import com.boclips.videos.service.testsupport.TestFactories.createCaptions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.util.Locale

class KalturaPlaybackProviderTest : AbstractSpringIntegrationTest() {

    @Test
    fun `return video with original dimensions`() {
        createMediaEntry(id = "1", width = 1280, height = 720)

        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "1")
        val playbackById = kalturaPlaybackProvider.retrievePlayback(listOf(playbackId))
        val playback = playbackById[playbackId]

        assertThat(playback?.originalDimensions).isEqualTo(
            Dimensions(
                1280,
                720
            )
        )
    }

    @Test
    fun `return video with assets`() {
        createMediaEntry(id = "1", assets = setOf(KalturaFactories.createKalturaAsset()))
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "1")
        val playbackById = kalturaPlaybackProvider.retrievePlayback(listOf(playbackId))

        val videoPlayback = playbackById[playbackId] as StreamPlayback

        assertThat(videoPlayback.assets).hasSize(1)
    }

    @Test
    fun `ignores 0-byte assets`() {
        createMediaEntry(id = "1", assets = setOf(KalturaFactories.createKalturaAsset(size = 0)))
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "1")
        val playbackById = kalturaPlaybackProvider.retrievePlayback(listOf(playbackId))

        val videoPlayback = playbackById[playbackId] as StreamPlayback

        assertThat(videoPlayback.assets).isEmpty()
    }

    @Test
    fun `list of assets is empty when no assets in kaltura`() {
        createMediaEntry(id = "1")

        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "1")
        val playbackById = kalturaPlaybackProvider.retrievePlayback(listOf(playbackId))

        val videoPlayback = playbackById[playbackId] as StreamPlayback

        assertThat(videoPlayback.assets).hasSize(0)
    }

    @Test
    fun `returns streaming information for videos`() {
        createMediaEntry(id = "1")

        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "1")
        val playbackById = kalturaPlaybackProvider.retrievePlayback(listOf(playbackId))

        assertThat(playbackById).hasSize(1)
        assertThat(playbackById[playbackId]).isNotNull

        val videoPlayback = playbackById[playbackId] as StreamPlayback
        assertThat(videoPlayback.downloadUrl).isEqualTo("https://download.com/entryId/1/format/download")
        assertThat(videoPlayback.duration).isEqualTo(Duration.parse("PT1M"))
    }

    @Test
    fun `returns several playbacks for videos based on entry ids`() {
        createMediaEntry(id = "1")
        createMediaEntry(id = "2")
        createMediaEntry(id = "3")

        val playbackIdOne = PlaybackId(type = PlaybackProviderType.KALTURA, value = "1")
        val playbackIdTwo = PlaybackId(type = PlaybackProviderType.KALTURA, value = "2")
        val playbackIdThree = PlaybackId(type = PlaybackProviderType.KALTURA, value = "3")

        val playbacksById = kalturaPlaybackProvider.retrievePlayback(
            listOf(
                playbackIdOne, playbackIdTwo, playbackIdThree
            )
        )

        assertThat(playbacksById).hasSize(3)
        assertThat(playbacksById[playbackIdOne]).isNotNull
        assertThat(playbacksById[playbackIdTwo]).isNotNull
        assertThat(playbacksById[playbackIdThree]).isNotNull
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
    fun `uploads captions if there are no captions in Kaltura already by entry id`() {
        val playbackId = mediaEntryWithCaptionsByEntryId(label = "Arabic", language = KalturaLanguage.ARABIC)

        val newEnglishCaptions = createCaptions(language = Locale.UK, content = "bla bla bla in english")
        kalturaPlaybackProvider.uploadCaptions(playbackId, newEnglishCaptions)

        val allCaptions = fakeKalturaClient.getCaptionsForVideo("entry-id")
        assertThat(allCaptions).hasSize(2)
        val englishCaptions = allCaptions.find { it.language != KalturaLanguage.ARABIC }!!
        assertThat(englishCaptions.language).isEqualTo(KalturaLanguage.ENGLISH)
        assertThat(englishCaptions.label).isEqualTo("English (auto-generated)")
        assertThat(englishCaptions.fileType).isEqualTo(CaptionFormat.WEBVTT)
        assertThat(fakeKalturaClient.getCaptionContent(englishCaptions.id)).isEqualTo("bla bla bla in english")
    }

    @Test
    fun `does not upload captions if there are manually-created captions in Kaltura already by entry id`() {
        val playbackId = mediaEntryWithCaptionsByEntryId("English")

        val newCaptions = createCaptions(language = Locale.UK)
        kalturaPlaybackProvider.uploadCaptions(playbackId, newCaptions)

        val allCaptions = fakeKalturaClient.getCaptionsForVideo("entry-id")
        assertThat(allCaptions).hasSize(1)
        assertThat(allCaptions.first().label).isEqualTo("English")
    }

    @Test
    fun `replaces captions if there are only automatically-created captions in Kaltura by entry id`() {
        val playbackId = mediaEntryWithCaptionsByEntryId("English (auto-generated)")

        kalturaPlaybackProvider.uploadCaptions(
            playbackId,
            createCaptions(language = Locale.UK, autoGenerated = true, content = "new captions")
        )

        val allCaptions = fakeKalturaClient.getCaptionsForVideo("entry-id")
        assertThat(allCaptions).hasSize(1)
        assertThat(fakeKalturaClient.getCaptionContent(allCaptions.first().id)).isEqualTo("new captions")
    }

    @Test
    fun `replaces only caption content for caption content updates`() {
        val playbackId = mediaEntryWithCaptionsByEntryId(
            label = "English (auto-generated)",
            language = KalturaLanguage.ENGLISH,
            entryId = "entry-id",
            captionContent = "Old Captions"
        )

        kalturaPlaybackProvider.overwriteCaptionContent(
            playbackId,
            "new captions"
        )

        val newCaptions = fakeKalturaClient.getCaptionsForVideo("entry-id")
        assertThat(newCaptions).hasSize(1)
        assertThat(fakeKalturaClient.getCaptionContent(newCaptions.first().id)).isEqualTo("new captions")
        assertThat(newCaptions.first().label).isEqualTo("English (auto-generated)")
        assertThat(newCaptions.first().language).isEqualTo(KalturaLanguage.ENGLISH)
    }

    @Test
    fun `retrieves captions by playback id`() {
        val playbackId = mediaEntryWithCaptionsByEntryId(
            label = "English (auto-generated)",
            language = KalturaLanguage.ENGLISH,
            entryId = "entry-id",
            captionContent = "Captions content to retrieve",
            format = CaptionFormat.WEBVTT

        )

        val captions = kalturaPlaybackProvider.getCaptions(playbackId)

        assertThat(captions).containsExactly(
            Caption(
                content = "Captions content to retrieve",
                format = WEBVTT,
                default = false
            )
        )
    }

    @Test
    fun `fetches human-generated captions by playback id`() {
        val playbackId = mediaEntryWithCaptionsByEntryId(
            label = "English",
            language = KalturaLanguage.ENGLISH,
            entryId = "entry-id",
            captionContent = "Captions content to retrieve",
            format = CaptionFormat.WEBVTT
        )

        val captionsUrl = kalturaPlaybackProvider.getHumanGeneratedCaption(playbackId)

        assertThat(captionsUrl?.downloadUrl?.path).isNotNull
    }

    @Test
    fun `auto-generated captions are ignored`() {
        val playbackId = mediaEntryWithCaptionsByEntryId(
            label = "English (auto-generated)",
            language = KalturaLanguage.ENGLISH,
            entryId = "entry-id",
            captionContent = "Captions content to retrieve",
            format = CaptionFormat.SRT
        )

        val captionsUrl = kalturaPlaybackProvider.getHumanGeneratedCaption(playbackId)

        assertThat(captionsUrl?.downloadUrl?.path).isNull()
    }

    @Test
    fun `deletes auto-generated captions when null`() {
        val playbackId = mediaEntryWithCaptionsByEntryId("English (auto-generated)")

        kalturaPlaybackProvider.deleteAutoGeneratedCaptions(playbackId, Locale.ENGLISH)

        val allCaptions = fakeKalturaClient.getCaptionsForVideo(playbackId.value)
        assertThat(allCaptions).isEmpty()
    }

    @Test
    fun `requests captions when not available`() {
        createMediaEntry("123")

        kalturaPlaybackProvider.requestCaptions(
            PlaybackId(
                value = "123",
                type = PlaybackProviderType.KALTURA
            )
        )

        val captionStatus = fakeKalturaClient.getCaptionStatus("123")
        assertThat(captionStatus).isEqualTo(KalturaCaptionManager.CaptionStatus.REQUESTED)
    }

    @Test
    fun `does request captions when no human generated captions available`() {
        val playbackId = mediaEntryWithCaptionsByEntryId("English (auto-generated)")

        kalturaPlaybackProvider.requestCaptions(playbackId)

        val captionStatus = fakeKalturaClient.getCaptionStatus(playbackId.value)
        assertThat(captionStatus).isEqualTo(KalturaCaptionManager.CaptionStatus.REQUESTED)
    }

    @Test
    fun `throws when human generated captions are already available`() {
        val playbackId = mediaEntryWithCaptionsByEntryId("English")

        assertThrows<CaptionConflictException> {
            kalturaPlaybackProvider.requestCaptions(playbackId)
        }
    }

    @Test
    fun `throws when human generated captions are already requested`() {
        fakeKalturaClient.createEntry("playback-id")
        fakeKalturaClient.tag("playback-id", listOf("caption48"))


        assertThrows<CaptionConflictException> {
            kalturaPlaybackProvider.requestCaptions(PlaybackId(PlaybackProviderType.KALTURA, "playback-id"))
        }
    }

    @Test
    fun `throws when human generated captions are already processing`() {
        fakeKalturaClient.createEntry("playback-id")
        fakeKalturaClient.tag("playback-id", listOf("processing"))

        assertThrows<CaptionConflictException> {
            kalturaPlaybackProvider.requestCaptions(PlaybackId(PlaybackProviderType.KALTURA, "playback-id"))
        }
    }

    private fun mediaEntryWithCaptionsByEntryId(
        label: String,
        language: KalturaLanguage = KalturaLanguage.ENGLISH,
        entryId: String = "entry-id",
        captionContent: String = "old captions",
        format: CaptionFormat = CaptionFormat.SRT
    ): PlaybackId {
        val existingCaptions = createKalturaCaptionAsset(label = label, language = language, captionFormat = format)
        createMediaEntry(id = entryId)
        fakeKalturaClient.createCaptionForVideo(entryId, existingCaptions, captionContent)
        return PlaybackId(type = PlaybackProviderType.KALTURA, value = entryId)
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

    @Test
    fun `download asset throws if entry not found`() {
        assertThrows<VideoPlaybackNotFound> {
            kalturaPlaybackProvider.downloadHighestResolutionVideo(PlaybackId.from("something-dude", "KALTURA"), ByteArrayOutputStream())
        }
    }

    @Test
    fun `download asset throws if no asset found`() {
        createMediaEntry(
            id = "2", height = 1080, assets = setOf()
        )

        assertThrows<VideoPlaybackNotFound> {
            kalturaPlaybackProvider.downloadHighestResolutionVideo(PlaybackId.from("2", "KALTURA"), ByteArrayOutputStream())
        }
    }

    @Test
    fun `download asset writes asset if available`() {
        val server = MockRestServiceServer.createServer(kalturaPlaybackProvider.restTemplate)
        createMediaEntry(
            id = "2", height = 1080, assets = setOf(
                KalturaFactories.createKalturaAsset(id = "x", height = 100),
                KalturaFactories.createKalturaAsset(id = "asset-id", height = 1080),
                KalturaFactories.createKalturaAsset(id = "y", height = 200)
            )
        )
        server.expect(requestTo("/asset-download/asset-id.mp4"))
            .andRespond(withSuccess("a video".toByteArray(), null))

        val os = ByteArrayOutputStream()
        kalturaPlaybackProvider.downloadHighestResolutionVideo(PlaybackId.from("2", "KALTURA"), os)

        assertThat(os.toByteArray()).isEqualTo("a video".toByteArray())
    }

    @Test
    fun `asset extension`() {
        createMediaEntry(
            id = "2", height = 1080, assets = setOf(
                KalturaFactories.createKalturaAsset(id = "x", height = 100),
                KalturaFactories.createKalturaAsset(id = "asset-id", height = 1080),
                KalturaFactories.createKalturaAsset(id = "y", height = 200)
            )
        )

        val extensionForAsset = kalturaPlaybackProvider.getExtensionForAsset(PlaybackId.from("2", "KALTURA"))

        assertThat(extensionForAsset).isEqualTo("mp4")
    }
}

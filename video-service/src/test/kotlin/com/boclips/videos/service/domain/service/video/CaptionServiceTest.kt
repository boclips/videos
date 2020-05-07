package com.boclips.videos.service.domain.service.video

import com.boclips.kalturaclient.captionasset.CaptionFormat as KalturaCaptionFormat
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.CaptionFormat.*
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.KalturaFactories.createKalturaCaptionAsset
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.VideoFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class CaptionServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var captionService: CaptionService

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `retrieves the caption content of a video`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id"))
        val existingCaptions = createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English (auto-generated)"
        )
        fakeKalturaClient.createCaptionForVideo("playback-id", existingCaptions, "captions content to retrieve")

        assertThat(captionService.getCaptionContent(videoId)).isEqualTo("captions content to retrieve")
    }

    @Test
    fun `retrieves full caption details of a video`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id"))
        val existingCaptions = createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English (auto-generated)",
            captionFormat = KalturaCaptionFormat.SRT
        )
        fakeKalturaClient.createCaptionForVideo("playback-id", existingCaptions, "captions content to retrieve")

        assertThat(captionService.getAvailableCaptions(videoId)).containsExactly(
            Caption(
                content = "captions content to retrieve",
                format = SRT
            )
        )
    }

    @Test
    fun `throws when attempting to fetch captions for a non existing video`() {
        assertThrows<VideoNotFoundException> { captionService.getAvailableCaptions(TestFactories.createVideoId()) }
    }

    @Test
    fun `Updates the caption content of a video`() {
        val video = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id"))
        val existingCaptions = createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English (auto-generated)"
        )
        fakeKalturaClient.createCaptionForVideo("playback-id", existingCaptions, "previous captions content")

        captionService.updateCaptionContent(video, """WEBVTT FILE

                        1
                        00:01.981 --> 00:04.682
                        We're quite content to be the odd<br>browser out.

                        2
                        00:05.302 --> 00:08.958
                        We don't have a fancy stock abbreviation <br>to go alongside our name in the press.

                        3
                        00:09.526 --> 00:11.324
                        We don't have a profit margin.""".trimIndent())

        val captionFiles = fakeKalturaClient.getCaptionsForVideo("playback-id")

        assertThat(captionFiles).hasSize(1)
        assertThat(fakeKalturaClient.getCaptionContent(captionFiles.first().id)).isEqualTo("""WEBVTT FILE

                        1
                        00:01.981 --> 00:04.682
                        We're quite content to be the odd<br>browser out.

                        2
                        00:05.302 --> 00:08.958
                        We don't have a fancy stock abbreviation <br>to go alongside our name in the press.

                        3
                        00:09.526 --> 00:11.324
                        We don't have a profit margin.""".trimIndent())

    }

    @Test
    fun `Updating a video's captions also updates its transcripts`() {
        val video = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id"))
        val existingCaptions = createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English (auto-generated)"
        )
        fakeKalturaClient.createCaptionForVideo("playback-id", existingCaptions, "previous captions content")

        captionService.updateCaptionContent(video, """WEBVTT FILE

                        1
                        00:01.981 --> 00:04.682
                        We're quite content to be the odd<br>browser out.

                        2
                        00:05.302 --> 00:08.958
                        We don't have a fancy stock abbreviation <br>to go alongside our name in the press.

                        3
                        00:09.526 --> 00:11.324
                        We don't have a profit margin.""".trimIndent())

        val updatedVideo = videoRepository.find(video)

        assertThat(updatedVideo?.transcript).isEqualTo("""
                        We're quite content to be the odd<br>browser out.
                        We don't have a fancy stock abbreviation <br>to go alongside our name in the press.
                        We don't have a profit margin.""".trimIndent())
    }

}

package com.boclips.videos.service.application.video

import com.boclips.eventbus.domain.video.CaptionsFormat
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.InsufficientVideoResolutionException
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class GetVideoUrlAssetsTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var getVideoUrlAssets: GetVideoUrlAssets

    @Test
    fun `returns captions & video urls`() {
        val playbackId = PlaybackId.from("playback-id", PlaybackProviderType.KALTURA.toString())
        val videoId = saveVideo(playbackId = playbackId)

        val captions = TestFactories.createCaptions(language = Locale.UK, content = "bla bla bla", format = CaptionsFormat.SRT)
        kalturaPlaybackProvider.uploadCaptions(playbackId, captions)

        val assetURLs = getVideoUrlAssets(videoId = videoId.value, user = UserFactory.sample())

        Assertions.assertThat(assetURLs.downloadVideoUrl).isNotNull
        Assertions.assertThat(assetURLs.downloadCaptionUrl).isNotNull
    }

    @Test
    fun `returns only video url when no srt captions available`() {
        val playbackId = PlaybackId.from("playback-id", PlaybackProviderType.KALTURA.toString())
        val videoId = saveVideo(playbackId = playbackId)

        val captionsWebvtt = TestFactories.createCaptions(language = Locale.UK, content = "bla bla bla", format = CaptionsFormat.WEBVTT)
        kalturaPlaybackProvider.uploadCaptions(playbackId, captionsWebvtt)

        val captionsCap = TestFactories.createCaptions(language = Locale.UK, content = "bla bla bla", format = CaptionsFormat.CAP)
        kalturaPlaybackProvider.uploadCaptions(playbackId, captionsCap)

        val captionsDfxp = TestFactories.createCaptions(language = Locale.UK, content = "bla bla bla", format = CaptionsFormat.DFXP)
        kalturaPlaybackProvider.uploadCaptions(playbackId, captionsDfxp)

        val captionsVtt = TestFactories.createCaptions(language = Locale.UK, content = "bla bla bla", format = CaptionsFormat.VTT)
        kalturaPlaybackProvider.uploadCaptions(playbackId, captionsVtt)

        val assetURLs = getVideoUrlAssets(videoId = videoId.value, user = UserFactory.sample())

        Assertions.assertThat(assetURLs.downloadVideoUrl).isNotNull
        Assertions.assertThat(assetURLs.downloadCaptionUrl).isNull()
    }

    @Test
    fun `throws exception when no video with given id`() {
        val videoId = TestFactories.createVideoId()

        assertThrows<VideoNotFoundException> {
            getVideoUrlAssets(videoId = videoId.value, user = UserFactory.sample())
        }
    }

    @Test
    fun `throws exception when no video assets assigned to video`() {
        val videoId = saveVideo(assets = emptySet())

        assertThrows<InsufficientVideoResolutionException> {
            getVideoUrlAssets(videoId = videoId.value, user = UserFactory.sample())
        }
    }

    @Test
    fun `throws exception when playback not from Kaltura`() {
        val playbackId = PlaybackId.from("playback-id", PlaybackProviderType.YOUTUBE.toString())
        val videoId = saveVideo(playbackId = playbackId)

        assertThrows<VideoPlaybackNotFound> {
            getVideoUrlAssets(videoId = videoId.value, user = UserFactory.sample())
        }
    }

    @Test
    fun `returns only video url when no captions for playback`() {
        val videoId = saveVideo()

        val assetURLs = getVideoUrlAssets(videoId = videoId.value, user = UserFactory.sample())

        Assertions.assertThat(assetURLs.downloadVideoUrl).isNotNull
        Assertions.assertThat(assetURLs.downloadCaptionUrl).isNull()
    }
}

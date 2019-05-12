package com.boclips.videos.service.infrastructure.video.mongo.converters

import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

class PlaybackConverterTest {

    @Test
    fun `convert valid Kaltura document`() {
        val originalPlayback = TestFactories.createKalturaPlayback(
            duration = Duration.ofSeconds(100),
            downloadUrl = "download",
            playbackId = "1234",
            thumbnailUrl = "thumbnail",
            dashStreamUrl = "dash",
            hlsStreamUrl = "hls",
            progressiveStreamUrl = "progressive"
        )

        val playbackDocument = PlaybackConverter.toDocument(originalPlayback)!!
        assertThat(playbackDocument.id).isEqualTo("1234")
        assertThat(playbackDocument.thumbnailUrls).containsExactly("thumbnail")
        assertThat(playbackDocument.downloadUrl).isEqualTo("download")
        assertThat(playbackDocument.hlsStreamUrl).isEqualTo("hls")
        assertThat(playbackDocument.dashStreamUrl).isEqualTo("dash")
        assertThat(playbackDocument.progressiveStreamUrl).isEqualTo("progressive")
        assertThat(playbackDocument.duration).isEqualTo(100)
        assertThat(playbackDocument.lastVerified).isNotNull()

        val convertedPlayback = PlaybackConverter.toPlayback(playbackDocument)
        assertThat(convertedPlayback).isEqualTo(originalPlayback)
    }

    @Test
    fun `convert valid Youtube document`() {
        val originalPlayback = TestFactories.createYoutubePlayback(
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "some-yt-id"),
            duration = Duration.ofSeconds(100),
            thumbnailUrl = "thumbnail-url"
        )

        val playbackDocument = PlaybackConverter.toDocument(originalPlayback)!!
        assertThat(playbackDocument.id).isEqualTo("some-yt-id")
        assertThat(playbackDocument.thumbnailUrls).containsExactly("thumbnail-url")
        assertThat(playbackDocument.downloadUrl).isNull()
        assertThat(playbackDocument.hlsStreamUrl).isNull()
        assertThat(playbackDocument.dashStreamUrl).isNull()
        assertThat(playbackDocument.progressiveStreamUrl).isNull()
        assertThat(playbackDocument.duration).isEqualTo(100)
        assertThat(playbackDocument.lastVerified).isNotNull()

        val convertedPlayback = PlaybackConverter.toPlayback(playbackDocument)
        assertThat(convertedPlayback).isEqualTo(originalPlayback)
    }
}
package com.boclips.videos.service.presentation.converters

import com.boclips.kalturaclient.clients.TestKalturaClient
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.request.video.YoutubePlaybackResource
import com.boclips.videos.service.presentation.hateoas.EventsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.PlaybacksLinkBuilder
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PlaybackToResourceConverterTest {

    private lateinit var eventsLinkBuilder: EventsLinkBuilder
    private lateinit var realPlaybacksLinkBuilder: PlaybacksLinkBuilder
    private lateinit var playbacksLinkBuilder: PlaybacksLinkBuilder
    private lateinit var playbackToResourceConverter: PlaybackToResourceConverter

    @BeforeEach
    fun setUp() {
        eventsLinkBuilder = mock()
        realPlaybacksLinkBuilder = PlaybacksLinkBuilder(TestKalturaClient())
        playbacksLinkBuilder = spy(realPlaybacksLinkBuilder)
        playbackToResourceConverter =
            PlaybackToResourceConverter(
                eventsLinkBuilder = eventsLinkBuilder,
                playbacksLinkBuilder = playbacksLinkBuilder
            )
    }

    val kalturaPlayback = TestFactories.createKalturaPlayback(downloadUrl = "https://download-url.com")

    val youtubePlayback = TestFactories.createYoutubePlayback()

    @Test
    fun `converts a resource from a Kaltura playback`() {
        val resource = this.playbackToResourceConverter.convert(kalturaPlayback)
        val content = resource as StreamPlaybackResource

        assertThat(content.duration).isEqualTo(kalturaPlayback.duration)
        assertThat(content.downloadUrl).isEqualTo("https://download-url.com")
        assertThat(content.id).isEqualTo(kalturaPlayback.id.value)
        assertThat(content.referenceId).isEqualTo(kalturaPlayback.referenceId)

        verify(eventsLinkBuilder).createPlaybackEventLink()
        verify(eventsLinkBuilder).createPlayerInteractedWithEventLink()
        verify(playbacksLinkBuilder).downloadLink(kalturaPlayback)
        verify(playbacksLinkBuilder).thumbnailLink(kalturaPlayback)
        verify(playbacksLinkBuilder).editThumbnailLink(kalturaPlayback)
        verify(playbacksLinkBuilder).videoPreviewLink(kalturaPlayback)
        verify(playbacksLinkBuilder).hlsStreamLink(kalturaPlayback)
    }

    @Test
    fun `converts a resource from a Youtube playback`() {
        val resource =
            this.playbackToResourceConverter.convert(youtubePlayback) as YoutubePlaybackResource

        assertThat(resource.duration).isEqualTo(youtubePlayback.duration)
        assertThat(resource.id).isEqualTo(youtubePlayback.id.value)

        verify(eventsLinkBuilder).createPlaybackEventLink()
        verify(eventsLinkBuilder).createPlayerInteractedWithEventLink()
        verify(playbacksLinkBuilder).thumbnailLink(youtubePlayback)
    }
}

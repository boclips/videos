package com.boclips.videos.service.presentation.converters

import com.boclips.kalturaclient.clients.TestKalturaClient
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.request.video.YoutubePlaybackResource
import com.boclips.videos.service.domain.model.playback.Dimensions
import com.boclips.videos.service.presentation.hateoas.EventsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.PlaybacksLinkBuilder
import com.boclips.videos.service.testsupport.KalturaFactories
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.VideoFactory
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.never

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

    val kalturaPlayback = TestFactories.createKalturaPlayback(
        downloadUrl = "https://download-url.com",
        thumbnailSecond = 10,
        originalDimensions = Dimensions(100,100),
        assets = setOf(VideoFactory.createVideoAsset(dimensions = Dimensions(100,100)))
    )

    val youtubePlayback = TestFactories.createYoutubePlayback()

    @Test
    fun `converts a resource from a Kaltura playback`() {
        val videoId = TestFactories.createVideoId()
        val resource = this.playbackToResourceConverter.convert(kalturaPlayback, videoId)
        val content = resource as StreamPlaybackResource

        assertThat(content.duration).isEqualTo(kalturaPlayback.duration)
        assertThat(content.downloadUrl).isEqualTo("https://download-url.com")
        assertThat(content.id).isEqualTo(kalturaPlayback.id.value)
        assertThat(content.referenceId).isEqualTo(kalturaPlayback.referenceId)
        assertThat(content.maxResolutionAvailable).isEqualTo(true)

        verify(eventsLinkBuilder).createPlaybackEventLink()
        verify(eventsLinkBuilder).createPlayerInteractedWithEventLink()
        verify(playbacksLinkBuilder).downloadLink(kalturaPlayback)
        verify(playbacksLinkBuilder).thumbnailLink(kalturaPlayback)
        verify(playbacksLinkBuilder).setThumbnail(kalturaPlayback, videoId)
        verify(playbacksLinkBuilder).setCustomThumbnail(kalturaPlayback, videoId)
        verify(playbacksLinkBuilder).videoPreviewLink(kalturaPlayback)
        verify(playbacksLinkBuilder).hlsStreamLink(kalturaPlayback)
    }

    @Test
    fun `converts a resource from a Youtube playback`() {
        val videoId = TestFactories.createVideoId()
        val resource =
            this.playbackToResourceConverter.convert(youtubePlayback, videoId) as YoutubePlaybackResource

        assertThat(resource.duration).isEqualTo(youtubePlayback.duration)
        assertThat(resource.id).isEqualTo(youtubePlayback.id.value)

        verify(eventsLinkBuilder).createPlaybackEventLink()
        verify(eventsLinkBuilder).createPlayerInteractedWithEventLink()
        verify(playbacksLinkBuilder).thumbnailLink(youtubePlayback)

        assertThat(playbacksLinkBuilder.setCustomThumbnail(youtubePlayback, videoId)).isNull()
        assertThat(playbacksLinkBuilder.setThumbnail(youtubePlayback, videoId)).isNull()
    }
}

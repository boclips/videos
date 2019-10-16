package com.boclips.videos.service.presentation.video

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.videos.service.presentation.hateoas.EventsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.PlaybacksLinkBuilder
import com.boclips.videos.service.presentation.video.playback.StreamPlaybackResource
import com.boclips.videos.service.presentation.video.playback.YoutubePlaybackResource
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
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
        playbackToResourceConverter = PlaybackToResourceConverter(
            eventsLinkBuilder = eventsLinkBuilder,
            playbacksLinkBuilder = playbacksLinkBuilder
        )
    }

    val kalturaPlayback = TestFactories.createKalturaPlayback()

    val youtubePlayback = TestFactories.createYoutubePlayback()

    @Test
    fun `converts a resource from a Kaltura playback`() {
        val resource = this.playbackToResourceConverter.wrapPlaybackInResource(kalturaPlayback)
        val content = resource.content as StreamPlaybackResource

        assertThat(content.streamUrl).isEqualTo("https://cdnapisec.kaltura.com/p/partner-id/sp/partner-id00/playManifest/entryId/entry-id/format/applehttp/flavorParamIds/1%2C2%2C3%2C4/protocol/https/video.mp4")
        assertThat(content.duration).isEqualTo(kalturaPlayback.duration)
        assertThat(content.thumbnailUrl).isEqualTo("https://cdnapisec.kaltura.com/p/partner-id/thumbnail/entry_id/entry-id/width/500/vid_slices/3/vid_slice/1")
        assertThat(content.id).isEqualTo(kalturaPlayback.id.value)
        assertThat(content.referenceId).isEqualTo(kalturaPlayback.referenceId)

        verify(eventsLinkBuilder).createPlaybackEventLink()
        verify(eventsLinkBuilder).createPlayerInteractedWithEventLink()
        verify(playbacksLinkBuilder, times(2)).thumbnailLink(kalturaPlayback)
        verify(playbacksLinkBuilder).videoPreviewLink(kalturaPlayback)
        verify(playbacksLinkBuilder, times(2)).hlsStreamLink(kalturaPlayback)
    }

    @Test
    fun `converts a resource from a Youtube playback`() {
        val resource =
            this.playbackToResourceConverter.wrapPlaybackInResource(youtubePlayback).content as YoutubePlaybackResource

        assertThat(resource.duration).isEqualTo(youtubePlayback.duration)
        assertThat(resource.thumbnailUrl).isEqualTo(youtubePlayback.thumbnailUrl)
        assertThat(resource.id).isEqualTo(youtubePlayback.id.value)

        verify(eventsLinkBuilder).createPlaybackEventLink()
        verify(eventsLinkBuilder).createPlayerInteractedWithEventLink()
        verify(playbacksLinkBuilder).thumbnailLink(youtubePlayback)
    }
}

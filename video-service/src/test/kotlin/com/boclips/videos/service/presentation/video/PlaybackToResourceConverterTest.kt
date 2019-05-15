package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.presentation.hateoas.EventsLinkBuilder
import com.boclips.videos.service.presentation.video.playback.StreamPlaybackResource
import com.boclips.videos.service.presentation.video.playback.YoutubePlaybackResource
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PlaybackToResourceConverterTest {

    private lateinit var eventsLinkBuilder: EventsLinkBuilder
    private lateinit var playbackToResourceConverter: PlaybackToResourceConverter

    @BeforeEach
    fun setUp() {
        eventsLinkBuilder = mock()
        playbackToResourceConverter = PlaybackToResourceConverter(eventsLinkBuilder)
    }

    val kalturaPlayback = TestFactories.createKalturaPlayback()

    val youtubePlayback = TestFactories.createYoutubePlayback()



    @Test
    fun `converts a resource from a Kaltura playback`() {
        val resource = this.playbackToResourceConverter.wrapPlaybackInResource(kalturaPlayback).content as StreamPlaybackResource

        assertThat(resource.streamUrl).isEqualTo(kalturaPlayback.appleHlsStreamUrl)
        assertThat(resource.duration).isEqualTo(kalturaPlayback.duration)
        assertThat(resource.thumbnailUrl).isEqualTo(kalturaPlayback.thumbnailUrl)
        assertThat(resource.id).isEqualTo(kalturaPlayback.id.value)
    }

    @Test
    fun `converts a resource from a Youtube playback`() {
        val resource = this.playbackToResourceConverter.wrapPlaybackInResource(youtubePlayback).content as YoutubePlaybackResource

        assertThat(resource.duration).isEqualTo(youtubePlayback.duration)
        assertThat(resource.thumbnailUrl).isEqualTo(youtubePlayback.thumbnailUrl)
        assertThat(resource.id).isEqualTo(youtubePlayback.id.value)
    }
}
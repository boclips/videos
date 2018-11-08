package com.boclips.videos.service.infrastructure.playback

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class YoutubePlaybackProviderTest {

    @Test
    fun `retrievePlayback does not call youtube when the id list is empty`() {

        val playbackProvider = YoutubePlaybackProvider("not a valid api key")

        val playbackById = playbackProvider.retrievePlayback(emptyList())

        assertThat(playbackById).isEmpty()
    }
}
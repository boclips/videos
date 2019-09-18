package com.boclips.videos.service.domain.model.playback

import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlaybackIdTest {

    @Test
    fun `it should return a KALTURA PlaybackId when KALTURA and entryId is provided`() {
        val playbackId = PlaybackId.from(
            playbackProviderName = PlaybackProviderType.KALTURA.name,
            playbackId = "entry-123"
        )

        assertThat(playbackId.type).isEqualTo(PlaybackProviderType.KALTURA)
        assertThat(playbackId.value).isEqualTo("entry-123")
    }

    @Test
    fun `it should return a YOUTUBE PlaybackId when YOUTUBE and playbackId is provided`() {
        val playbackId = PlaybackId.from(
            playbackProviderName = PlaybackProviderType.YOUTUBE.name,
            playbackId = "youtube-123"
        );

        assertThat(playbackId.type).isEqualTo(PlaybackProviderType.YOUTUBE)
        assertThat(playbackId.value).isEqualTo("youtube-123")
    }

}
package com.boclips.videos.service.presentation.event

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class CreatePlaybackEventCommandTest {
    private val playbackEvent = CreatePlaybackEventCommand(
        videoId = "v678",
        videoIndex = 0,
        segmentStartSeconds = 10,
        segmentEndSeconds = 20
    )

    @Test
    fun `validates video identifier`() {
        Assertions.assertThatThrownBy { playbackEvent.copy(videoId = null).isValidOrThrows() }
        Assertions.assertThatThrownBy { playbackEvent.copy(videoId = "").isValidOrThrows() }
    }

    @Test
    fun `validates segmentStartSeconds`() {
        Assertions.assertThatThrownBy { playbackEvent.copy(segmentStartSeconds = null).isValidOrThrows() }
        Assertions.assertThatThrownBy { playbackEvent.copy(segmentStartSeconds = -1).isValidOrThrows() }
    }

    @Test
    fun `validates segmentEndSeconds`() {
        Assertions.assertThatThrownBy { playbackEvent.copy(segmentEndSeconds = null).isValidOrThrows() }
        Assertions.assertThatThrownBy { playbackEvent.copy(segmentEndSeconds = -1).isValidOrThrows() }
    }
}

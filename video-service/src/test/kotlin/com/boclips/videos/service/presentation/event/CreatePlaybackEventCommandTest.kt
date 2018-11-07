package com.boclips.videos.service.presentation.event

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class CreatePlaybackEventCommandTest {
    val playbackEvent = CreatePlaybackEventCommand(
            playerId = "player-id",
            videoId = "v678",
            segmentStartSeconds = 10,
            segmentEndSeconds = 20,
            videoDurationSeconds = 60,
            captureTime = "2018-01-01T00:00:00.000Z",
            searchId = "search-id"
    )

    @Test
    fun `validates player identifier`() {
        Assertions.assertThatThrownBy { playbackEvent.copy(playerId = null).isValidOrThrows() }
        Assertions.assertThatThrownBy { playbackEvent.copy(playerId = "").isValidOrThrows() }
    }

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

    @Test
    fun `validates videoDurationSeconds`() {
        Assertions.assertThatThrownBy { playbackEvent.copy(videoDurationSeconds = null).isValidOrThrows() }
        Assertions.assertThatThrownBy { playbackEvent.copy(videoDurationSeconds = -1).isValidOrThrows() }
    }
}
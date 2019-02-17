package com.boclips.videos.service.presentation.event

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class CreatePlaybackEventCommandTest {
    private val playbackEvent = CreatePlaybackEventCommand(
        playerId = "player-id",
        assetId = "v678",
        videoIndex = 0,
        segmentStartSeconds = 10,
        segmentEndSeconds = 20,
        videoDurationSeconds = 60
    )

    @Test
    fun `validates player identifier`() {
        Assertions.assertThatThrownBy { playbackEvent.copy(playerId = null).isValidOrThrows() }
        Assertions.assertThatThrownBy { playbackEvent.copy(playerId = "").isValidOrThrows() }
    }

    @Test
    fun `validates video identifier`() {
        Assertions.assertThatThrownBy { playbackEvent.copy(assetId = null).isValidOrThrows() }
        Assertions.assertThatThrownBy { playbackEvent.copy(assetId = "").isValidOrThrows() }
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
package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.testsupport.CreatePlaybackEventCommandFactory
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class ValidateCreatePlaybackEventsKtTest {
    @Test
    fun `validate null list`() {
        org.junit.jupiter.api.assertThrows<InvalidEventException> {
            validateCreatePlaybackEvents(null)
        }
    }

    @Test
    fun `validate empty list`() {
        org.junit.jupiter.api.assertThrows<InvalidEventException> {
            validateCreatePlaybackEvents(emptyList())
        }
    }

    @Test
    fun `validate list with one item`() {
        val validEvent = CreatePlaybackEventCommandFactory.sample()

        validateCreatePlaybackEvents(listOf(validEvent))
    }

    @Test
    fun `invalidate list with one item`() {
        val invalidEvent = CreatePlaybackEventCommandFactory.sample(
            segmentEndSeconds = -1
        )

        org.junit.jupiter.api.assertThrows<InvalidEventException> {
            validateCreatePlaybackEvents(listOf(invalidEvent))
        }
    }

    @Test
    fun `validate list of items`() {
        val validEvent = CreatePlaybackEventCommandFactory.sample(captureTime = ZonedDateTime.now())

        validateCreatePlaybackEvents(listOf(validEvent, validEvent))
    }

    @Test
    fun `invalidate list of items`() {
        val validEvent = CreatePlaybackEventCommandFactory.sample()
        val invalidEvent = CreatePlaybackEventCommandFactory.sample(segmentEndSeconds = -1)

        org.junit.jupiter.api.assertThrows<InvalidEventException> {
            validateCreatePlaybackEvents(listOf(validEvent, invalidEvent))
        }
    }

    @Test
    fun `invalidate list of items because of missing timestamp`() {
        val validEvent = CreatePlaybackEventCommandFactory.sample()
        val invalidEvent = CreatePlaybackEventCommandFactory.sample(captureTime = null)

        org.junit.jupiter.api.assertThrows<InvalidEventException> {
            validateCreatePlaybackEvents(listOf(validEvent, invalidEvent))
        }
    }
}


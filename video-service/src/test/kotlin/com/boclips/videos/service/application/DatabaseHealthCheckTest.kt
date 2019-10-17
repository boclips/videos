package com.boclips.videos.service.application

import com.boclips.videos.service.domain.model.video.VideoRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.Status

class DatabaseHealthCheckTest {
    @Test
    fun `is down when video retrieval throws`() {
        val repo = mock<VideoRepository> {
            on { find(any()) } doThrow RuntimeException("maybe I can't connect?")
        }

        val check = DatabaseHealthCheck(repo)

        Assertions.assertThat(check.health().status).isEqualTo(Status("DOWN"))
    }

    @Test
    fun `is up when application can retrieve video without exception`() {
        val repo = mock<VideoRepository> {
            on { find(any()) } doReturn null
        }

        val check = DatabaseHealthCheck(repo)

        Assertions.assertThat(check.health().status).isEqualTo(Status("UP"))
    }
}

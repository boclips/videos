package com.boclips.videos.service.application

import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

import org.springframework.boot.actuate.health.Status

class SearchHealthCheckTest {
    @Test
    fun `is down when search throws`() {
        val searchService = mock<VideoSearchService> {
            on { count(any()) } doThrow RuntimeException("maybe I can't connect?")
        }

        val check = SearchHealthCheck(searchService)

        Assertions.assertThat(check.health().status).isEqualTo(Status("DOWN"))
    }

    @Test
    fun `is up when application can search without exception`() {
        val searchService = mock<VideoSearchService> {
            on { count(any()) } doReturn 0
        }

        val check = SearchHealthCheck(searchService)

        Assertions.assertThat(check.health().status).isEqualTo(Status("UP"))
    }
}

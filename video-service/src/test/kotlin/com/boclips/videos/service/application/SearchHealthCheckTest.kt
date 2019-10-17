package com.boclips.videos.service.application

import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.Status

class SearchHealthCheckTest {
    @Test
    fun `is down when video search throws`() {
        val searchService = mock<VideoSearchService> {
            on { makeSureIndexIsThere() } doThrow RuntimeException("maybe I can't connect?")
        }

        val check = SearchHealthCheck(searchService, mock<CollectionSearchService> {})

        Assertions.assertThat(check.health().status).isEqualTo(Status("DOWN"))
    }

    @Test
    fun `is down when collection search throws`() {
        val videoSearchService = mock<VideoSearchService> {}
        val collectionSearchService = mock<CollectionSearchService> {
            on { makeSureIndexIsThere() } doThrow RuntimeException("maybe I can't connect?")
        }

        val check = SearchHealthCheck(videoSearchService, collectionSearchService)

        Assertions.assertThat(check.health().status).isEqualTo(Status("DOWN"))
    }

    @Test
    fun `is up when application can connect to video search without exception`() {
        val check = SearchHealthCheck(mock<VideoSearchService> {}, mock<CollectionSearchService> {})

        Assertions.assertThat(check.health().status).isEqualTo(Status("UP"))
    }
}

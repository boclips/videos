package com.boclips.videos.service.application

import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.infrastructure.search.SearchService
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.Mockito.mock

class SearchVideosTest {

    @Test
    fun `execute throws an exception when query is null`() {
        val searchVideos = SearchVideos(mock(VideoService::class.java))

        assertThatThrownBy {
            searchVideos.execute(null)
        }.isInstanceOf(QueryValidationException::class.java)
    }
}
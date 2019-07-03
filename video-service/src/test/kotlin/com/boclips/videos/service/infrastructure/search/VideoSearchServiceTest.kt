package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoSearchServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var searchService: VideoSearchService

    @Test
    fun `upsert inserts videos`() {
        val videoNews = TestFactories.createVideo(
            videoId = TestFactories.aValidId(),
            title = "isNews",
            type = LegacyVideoType.NEWS
        )
        val videoStock = TestFactories.createVideo(
            videoId = TestFactories.aValidId(),
            title = "stock",
            type = LegacyVideoType.STOCK
        )

        searchService.upsert(sequenceOf(videoNews, videoStock))

        assertThat(
            searchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery("isNews"),
                    startIndex = 0,
                    windowSize = 10
                )
            )
        ).isNotEmpty
        assertThat(
            searchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery("stock"),
                    startIndex = 0,
                    windowSize = 10
                )
            )
        ).isNotEmpty
    }
}

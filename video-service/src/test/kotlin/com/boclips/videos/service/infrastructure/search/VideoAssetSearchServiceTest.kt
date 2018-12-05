package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.videos.service.domain.model.asset.VideoType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoAssetSearchServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var searchService: VideoAssetSearchService

    @Test
    fun `upsert inserts videos filtered for teachers`() {
        val videoNews = TestFactories.createVideoAsset(title = "news", type = VideoType.NEWS)
        val videoStock = TestFactories.createVideoAsset(title = "stock", type = VideoType.STOCK)

        searchService.upsert(sequenceOf(videoNews, videoStock))

        assertThat(searchService.search(PaginatedSearchRequest(query = "news", startIndex = 0, windowSize = 10))).isNotEmpty
        assertThat(searchService.search(PaginatedSearchRequest(query = "stock", startIndex = 0, windowSize = 10))).isEmpty()
    }
}
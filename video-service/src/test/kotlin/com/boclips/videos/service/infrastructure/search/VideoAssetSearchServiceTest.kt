package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.Query
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoAssetSearchServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var searchService: VideoAssetSearchService

    @Test
    fun `upsert inserts videos`() {
        val videoNews = TestFactories.createVideoAsset(videoId = TestFactories.aValidId(), title = "isNews", type = LegacyVideoType.NEWS)
        val videoStock = TestFactories.createVideoAsset(videoId = TestFactories.aValidId(), title = "stock", type = LegacyVideoType.STOCK)

        searchService.upsert(sequenceOf(videoNews, videoStock))

        assertThat(searchService.search(PaginatedSearchRequest(query = Query("isNews"), startIndex = 0, windowSize = 10))).isNotEmpty
        assertThat(searchService.search(PaginatedSearchRequest(query = Query("stock"), startIndex = 0, windowSize = 10))).isNotEmpty
    }
}

package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.service.video.VideoIndex
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoIndexTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var index: VideoIndex

    @Test
    fun `upsert inserts videos`() {
        val videoNews = TestFactories.createVideo(
            videoId = TestFactories.aValidId(),
            title = "isNews",
            types = listOf(ContentType.NEWS)
        )
        val videoStock = TestFactories.createVideo(
            videoId = TestFactories.aValidId(),
            title = "stock",
            types = listOf(ContentType.STOCK)
        )

        index.upsert(sequenceOf(videoNews, videoStock))

        assertThat(
            index.search(
                PaginatedSearchRequest(
                    query = VideoQuery("isNews"),
                    startIndex = 0,
                    windowSize = 10
                )
            ).elements
        ).isNotEmpty
        assertThat(
            index.search(
                PaginatedSearchRequest(
                    query = VideoQuery("stock"),
                    startIndex = 0,
                    windowSize = 10
                )
            ).elements
        ).isNotEmpty
    }
}

package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.video.VideoType
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
        val videoNews = TestFactories.createVideoWithPrices(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId(),
                title = "isNews",
                types = listOf(VideoType.NEWS)
            )
        )
        val videoStock = TestFactories.createVideoWithPrices(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId(),
                title = "stock",
                types = listOf(VideoType.STOCK)
            )
        )

        index.upsert(sequenceOf(videoNews, videoStock))

        assertThat(
            index.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery("isNews", videoAccessRuleQuery = VideoAccessRuleQuery()),
                    startIndex = 0,
                    windowSize = 10
                )
            ).elements
        ).isNotEmpty
        assertThat(
            index.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery("stock", videoAccessRuleQuery = VideoAccessRuleQuery()),
                    startIndex = 0,
                    windowSize = 10
                )
            ).elements
        ).isNotEmpty
    }
}

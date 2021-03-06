package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.infrastructure.videos.AccessRulesFilter.Companion.buildAccessRulesFilter
import org.assertj.core.api.Assertions
import org.elasticsearch.index.query.QueryBuilders
import org.junit.jupiter.api.Test

class AccessRulesFilterTest {
    @Test
    fun `creates access rule filters for given query`() {
        val videoQuery = VideoQuery(
            phrase = "apple",
            videoAccessRuleQuery = VideoAccessRuleQuery(
                excludedTypes = setOf(VideoType.STOCK, VideoType.NEWS),
                includedTypes = setOf(VideoType.INSTRUCTIONAL),
                excludedContentPartnerIds = setOf("CH1"),
                deniedVideoIds = setOf("badvid2"),
                isEligibleForStream = true,
                isEligibleForDownload = true,
                excludedSourceTypes = setOf(SourceType.YOUTUBE)
            )
        )
        val expectedQuery = AccessRulesFilterTest::class.java.classLoader.getResource("AccessRulesFilter.json")!!
            .readText()
            .trimEnd()

        val generatedQuery = buildAccessRulesFilter(QueryBuilders.boolQuery(), videoQuery.videoAccessRuleQuery).toString()
        Assertions.assertThat(generatedQuery).isEqualTo(expectedQuery)
    }
}

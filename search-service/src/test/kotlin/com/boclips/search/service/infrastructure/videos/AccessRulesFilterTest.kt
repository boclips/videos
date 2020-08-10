package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.AccessRuleQuery
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
            accessRuleQuery = AccessRuleQuery(
                excludedTypes = setOf(VideoType.STOCK, VideoType.NEWS),
                includedTypes = setOf(VideoType.INSTRUCTIONAL),
                excludedContentPartnerIds = setOf("CH1"),
                deniedVideoIds = setOf("badvid2"),
                isEligibleForStream = true
            )
        )
        val expectedQuery = AccessRulesFilterTest::class.java.classLoader.getResource("AccessRulesFilter.json")!!
            .readText()
            .trimEnd()

        val generatedQuery = buildAccessRulesFilter(QueryBuilders.boolQuery(), videoQuery.accessRuleQuery).toString()
        Assertions.assertThat(generatedQuery).isEqualTo(expectedQuery)
    }
}

package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class EsVideoQueryTest {
    @Test
    fun `creates video filters for given query`() {
        val videoQuery = VideoQuery(
            phrase = "\"oranges\" \"in orchard\" apple",
            videoAccessRuleQuery = VideoAccessRuleQuery(
                excludedTypes = setOf(VideoType.STOCK),
                includedTypes = setOf(VideoType.INSTRUCTIONAL),
                isEligibleForStream = true,
                excludedSourceTypes = setOf(SourceType.YOUTUBE)
            )
        )
        val expectedQuery = EsVideoQueryTest::class.java.classLoader.getResource("VideoQuery.json")!!
            .readText()
            .trimEnd()

        val generatedQuery = EsVideoQuery().buildQuery(videoQuery).toString()
        Assertions.assertThat(generatedQuery).isEqualTo(expectedQuery)
    }
}

package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.testsupport.VideoQueryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoFilterCriteriaTest {
    @Test
    fun `creates video filters given query`() {
        val videoQuery = VideoQueryFactory.aRandomExample()
        val expectedQuery = VideoFilterCriteriaTest::class.java.classLoader.getResource("VideoPostFilter.json")!!
            .readText()
            .trimEnd()

        val generatedQuery = VideoFilterCriteria.allCriteria(videoQuery.userQuery).toString()
        assertThat(generatedQuery).isEqualTo(expectedQuery)
    }
}

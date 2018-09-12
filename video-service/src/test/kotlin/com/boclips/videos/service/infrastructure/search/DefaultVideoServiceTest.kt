package com.boclips.videos.service.infrastructure.search

import com.boclips.videos.service.testsupport.ElasticSearchVideoFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DefaultVideoServiceTest {
    @Test
    fun `extract reference ids from search result`() {
        val videos = listOf(ElasticSearchVideoFactory.create(referenceId = "the-one"))
        val referenceIds = DefaultVideoService.extractKalturaReferenceIds(videos)

        assertThat(referenceIds).containsExactly("the-one")
    }
}
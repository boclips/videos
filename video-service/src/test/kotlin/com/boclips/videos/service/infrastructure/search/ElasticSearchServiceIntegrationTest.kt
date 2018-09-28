package com.boclips.videos.service.infrastructure.search

import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class ElasticSearchServiceIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var searchService: SearchService

    @Test
    fun search() {
        val result = searchService.search("powerful")

        assertThat(result[0].id).isEqualTo("test-id-3")
        assertThat(result[0].title).isNotBlank()
        assertThat(result[0].description).isNotBlank()
        assertThat(result[0].releasedOn).isNotNull()
        assertThat(result[0].contentProvider).isNotBlank()
    }

    @Test
    fun findById() {
        val result = searchService.findById("test-id-3")

        assertThat(result!!.id).isEqualTo("test-id-3")
        assertThat(result.title).isNotBlank()
        assertThat(result.description).isNotBlank()
        assertThat(result.releasedOn).isNotNull()
        assertThat(result.contentProvider).isNotBlank()
    }
}
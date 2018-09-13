package com.boclips.videos.service.infrastructure.search

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

        assertThat(result.videos).containsExactly(ElasticSearchVideo(
                id = "test-id-3",
                title = "powerful video about elephants",
                description = "test description 3",
                date = "2018-02-11",
                source = "cp",
                referenceId = "ref-id-3"
        ))
    }

    @Test
    fun findById() {
        val result = searchService.findById("test-id-3")

        assertThat(result).isEqualTo(ElasticSearchVideo(
                id = "test-id-3",
                title = "powerful video about elephants",
                description = "test description 3",
                date = "2018-02-11",
                source = "cp",
                referenceId = "ref-id-3"
        ))
    }
}
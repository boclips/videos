package com.boclips.videos.service.infrastructure.search

import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.service.SearchService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test", "search-test")
class ElasticSearchServiceIntegrationTest {

    @Autowired
    lateinit var searchService: SearchService

    @Test
    fun `finds a video with keyword in description`() {
        val result = searchService.search("powerful")

        assertThat(result[0].videoId).isEqualTo("390")
    }

    @Test
    fun `returns empty collection for empty result`() {
        val result = searchService.search("somethingthatdoesntexist")

        assertThat(result).hasSize(0)
    }

    @Test
    fun `remove entry from index`() {
        val deletedVideo = VideoId(videoId = "690")

        assertThat(searchService.isIndexed(deletedVideo)).isTrue()

        searchService.removeFromSearch(deletedVideo)

        assertThat(searchService.isIndexed(deletedVideo)).isFalse()
    }

}
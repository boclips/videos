package com.boclips.videos.service.presentation

import com.boclips.search.service.domain.Query
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asOperator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class E2EControllerTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var videoAssetRepository: VideoAssetRepository

    @BeforeEach
    fun setUp() {
        saveVideo(videoId = 123)
    }

    @Test
    fun `resets database and both search indices`() {
        assertThat(fakeSearchService.count(Query(ids = listOf("123")))).isEqualTo(1)
        assertThat(videoAssetRepository.find(AssetId(value = "123"))).isNotNull

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/e2e/actions/reset_all").asOperator())
                .andExpect(MockMvcResultMatchers.status().isOk)

        assertThat(fakeSearchService.count(Query(ids = listOf("123")))).isEqualTo(0)
        assertThat(videoAssetRepository.find(AssetId(value = "123"))).isNull()
    }
}

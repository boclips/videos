package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asOperator
import com.boclips.videos.service.testsupport.asTeacher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class AdminControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val videoId = saveVideo()
    }

    @Test
    fun `rebuildSearchIndex returns 403 when user is not allowed to reindex`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/rebuild_search_index").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `rebuildSearchIndex returns 200 OK when user is allowed to reindex`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/rebuild_search_index").asOperator())
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `buildLegacySearchIndex returns 403 when user is not allowed to reindex`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/build_legacy_search_index").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `buildLegacySearchIndex returns 200 OK when user is allowed to reindex`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/build_legacy_search_index").asOperator())
            .andExpect(MockMvcResultMatchers.status().isOk)
    }
}
package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asOperator
import com.boclips.videos.service.testsupport.asTeacher
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class AdminControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Before
    fun setUp() {
        saveVideo(videoId = 123,
                referenceId = "ref-id-1",
                title = "powerful video about elephants",
                description = "test description 3",
                date = "2018-02-11",
                duration = "00:01:00",
                contentProvider = "cp")
    }

    @Test
    fun `rebuildSearchIndex returns 403 when user is not allowed to reindex`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/rebuild_search_index").asTeacher())
                .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `rebuildSearchIndex returns 202 accepted when user is allowed to reindex`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/actions/rebuild_search_index").asOperator())
                .andExpect(MockMvcResultMatchers.status().isAccepted)
    }

}
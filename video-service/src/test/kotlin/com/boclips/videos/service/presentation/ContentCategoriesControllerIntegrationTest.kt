package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asApiUser
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ContentCategoriesControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

     @Test
     fun `returns available video types for authenticated API users`() {
         mockMvc.perform(get("/v1/content-categories").asApiUser())
             .andExpect(status().isOk)
             .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.contentCategories", Matchers.hasSize<Any>(19)))
             .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.contentCategories",
                 Matchers.containsInAnyOrder("360 and Virtual reality",
                     "Animation",
                     "Documentary shorts",
                     "Early childhood",
                     "Educational songs",
                     "Inspiration for lessons",
                     "Instructional videos",
                     "Interviews",
                     "Historical archive",
                     "Music",
                     "Narrated",
                     "News stories",
                     "Pratical experiments",
                     "Songs",
                     "Stock clips",
                     "Study skills",
                     "Sustainability",
                     "With a child host",
                     "With a host"
                     )
             ))
     }

    @Test
    fun `returns a 403 response for unauthenticated users`() {
        mockMvc.perform(get("/v1/content-categories"))
            .andExpect(status().isForbidden)
    }
}
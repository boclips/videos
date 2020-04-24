package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class SuggestionsControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `provides suggestions for content partner names`() {
        saveContentPartner(name = "TED")
        saveContentPartner(name = "TED-Ed")
        saveContentPartner(name = "BBC")

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/suggestions?query=ted").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.suggestionTerm", equalTo("ted")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contentPartners", hasSize<String>(2)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.contentPartners[0].name",
                    equalTo("TED")
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.contentPartners[0]._links.searchVideos")
                    .exists()
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.contentPartners[1].name",
                    equalTo("TED-Ed")
                )
            )
    }
}

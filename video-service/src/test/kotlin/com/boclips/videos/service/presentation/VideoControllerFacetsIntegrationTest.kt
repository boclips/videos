package com.boclips.videos.service.presentation

import com.boclips.search.service.domain.common.FacetType
import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.common.FacetCount
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VideoControllerFacetsIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `contains counts for subjects`() {
        videoSearchService.setFacets(
            listOf(
                FacetCount(
                    type = FacetType.Subjects,
                    counts = listOf(Count(id = "subject-1", hits = 56))
                )
            )
        )

        mockMvc.perform(get("/v1/videos?query=content").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.facets.subjects", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.facets.subjects[0].id", equalTo("subject-1")))
            .andExpect(jsonPath("$._embedded.facets.subjects[0].hits", equalTo(56)))
    }

    @Test
    fun `does not render if they don't exist`() {
        videoSearchService.setFacets(emptyList())

        mockMvc.perform(get("/v1/videos?query=content").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.facets.subjects[*]", hasSize<Int>(0)))
    }
}


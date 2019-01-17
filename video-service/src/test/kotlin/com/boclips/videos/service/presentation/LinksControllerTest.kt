package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.UriTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class LinksControllerTest : AbstractSpringIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `GET returns links`() {
        mockMvc.perform(get("/v1"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._links.search.href", containsString("/videos?query=")))
                .andExpect(jsonPath("$._links.search.href", containsString("{&include_tag,exclude_tag}")))
                .andExpect(jsonPath("$._links.search.templated", equalTo(true)))
                .andExpect(jsonPath("$._links.video.href", containsString("/videos/")))
                .andExpect(jsonPath("$._links.video.templated", equalTo(true)))
                .andExpect(jsonPath("$._links.createPlaybackEvent.href", endsWith("/events/playback")))
                .andExpect(jsonPath("$._links.createNoSearchResultsEvent.href", endsWith("/events/no-search-results")))
                .andExpect(jsonPath("$._links.userDefaultCollection.href", containsString("collections/default")))
    }

    @Test
    fun `GET links uses proto headers`() {
        mockMvc.perform(get("/v1").header("x-forwarded-proto", "https"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._links.search.href", startsWith("https")))
    }

    @Test
    fun `optional parameters are not required by link template`() {
        val response = mockMvc.perform(get("/v1")).andReturn().response.contentAsString

        val searchUrlTemplate: String = JsonPath.parse(response).read("$._links.search.href")
        val searchUrl = UriTemplate(searchUrlTemplate).expand(mapOf(("query" to "phrase"), ("size" to 1), ("page" to 1)))

        assertThat(searchUrl.toASCIIString()).endsWith("/videos?query=phrase&size=1&page=1")
    }
}

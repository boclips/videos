package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asTeacher
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.startsWith
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
            .andExpect(jsonPath("$._links.adminSearch").doesNotExist())
            .andExpect(jsonPath("$._links.search.href", containsString("/videos?query=")))
            .andExpect(jsonPath("$._links.search.href", containsString("{&include_tag,exclude_tag}")))
            .andExpect(jsonPath("$._links.search.templated", equalTo(true)))
            .andExpect(jsonPath("$._links.video.href", containsString("/videos/{id}")))
            .andExpect(jsonPath("$._links.video.templated", equalTo(true)))
            .andExpect(jsonPath("$._links.videos.href", endsWith("/videos")))
            .andExpect(jsonPath("$._links.createPlaybackEvent.href", endsWith("/events/playback")))
            .andExpect(jsonPath("$._links.createNoSearchResultsEvent.href", endsWith("/events/no-search-results")))
            .andExpect(jsonPath("$._links.userCollections.href", endsWith("collections")))
            .andExpect(jsonPath("$._links.userCollection.href", endsWith("collections/{id}")))
            .andExpect(jsonPath("$._links.userCollection.templated", equalTo(true)))
            .andExpect(jsonPath("$._links.userCollectionsDetails.href", endsWith("collections?projection=details&owner={owner}")))
            .andExpect(jsonPath("$._links.userCollectionsDetails.templated", equalTo(true)))
            .andExpect(jsonPath("$._links.userCollectionsList.href", endsWith("collections?projection=list&owner={owner}")))
            .andExpect(jsonPath("$._links.userCollectionsList.templated", equalTo(true)))
            .andExpect(jsonPath("$._links.publicCollections.href", endsWith("collections?projection=list&owner={owner}")))
    }

    @Test
    fun `when authenticated user`() {
        val userId = "teacher@teacher.com"
        mockMvc.perform(get("/v1").asTeacher(userId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.userCollectionsDetails.href", endsWith("collections?projection=details&owner=$userId")))
            .andExpect(jsonPath("$._links.userCollectionsList.href", endsWith("collections?projection=list&owner=$userId")))
    }

    @Test
    fun `when can view restricted videos GET returns admin search`() {
        mockMvc.perform(get("/v1").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.adminSearch.href", containsString("/videos/search")))
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
        val searchUrl =
            UriTemplate(searchUrlTemplate).expand(mapOf(("query" to "phrase"), ("size" to 1), ("page" to 1)))

        assertThat(searchUrl.toASCIIString()).endsWith("/videos?query=phrase&size=1&page=1")
    }
}

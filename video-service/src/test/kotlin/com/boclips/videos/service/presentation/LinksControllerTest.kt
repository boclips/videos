package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asSubjectClassifier
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
    fun `when not authenticated`() {
        mockMvc.perform(get("/v1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.video.href", containsString("/videos/{id}")))
            .andExpect(jsonPath("$._links.video.templated", equalTo(true)))
            .andExpect(jsonPath("$._links.createPlaybackEvent.href", endsWith("/events/playback")))
            .andExpect(jsonPath("$._links.createNoSearchResultsEvent.href", endsWith("/events/no-search-results")))
            .andExpect(
                jsonPath(
                    "$._links.publicCollections.href",
                    endsWith("collections?projection=list&public=true&page=0&size=30")
                )
            )
            .andExpect(jsonPath("$._links.subjects.href", endsWith("/subjects")))

            .andExpect(jsonPath("$._links.adminSearch").doesNotExist())
            .andExpect(jsonPath("$._links.search").doesNotExist())
            .andExpect(jsonPath("$._links.videos").doesNotExist())
            .andExpect(jsonPath("$._links.myCollections").doesNotExist())
            .andExpect(jsonPath("$._links.collectionsByOwner").doesNotExist())
            .andExpect(jsonPath("$._links.bookmarkedCollections").doesNotExist())
            .andExpect(jsonPath("$._links.collection").doesNotExist())
            .andExpect(jsonPath("$._links.collections").doesNotExist())
    }

    @Test
    fun `when authenticated user`() {
        val userId = "teacher@teacher.com"
        mockMvc.perform(get("/v1").asTeacher(userId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.search.href", containsString("/videos?query=")))
            .andExpect(
                jsonPath(
                    "$._links.search.href",
                    containsString("{&sort_by,include_tag,exclude_tag,min_duration,max_duration,released_date_from,released_date_to,source}")
                )
            )
            .andExpect(jsonPath("$._links.search.templated", equalTo(true)))
            .andExpect(jsonPath("$._links.subjects.href", endsWith("/subjects")))
            .andExpect(
                jsonPath(
                    "$._links.publicCollections.href",
                    endsWith("collections?projection=list&public=true&page=0&size=30")
                )
            )
            .andExpect(
                jsonPath(
                    "$._links.bookmarkedCollections.href",
                    endsWith("collections?projection=list&public=true&bookmarked=true&page=0&size=30")
                )
            )
            .andExpect(
                jsonPath(
                    "$._links.myCollections.href",
                    endsWith("collections?projection=list&page=0&size=30&owner=teacher@teacher.com")
                )
            )
            .andExpect(jsonPath("$._links.collectionsByOwner").doesNotExist())
            .andExpect(jsonPath("$._links.collection.href", endsWith("collections/{id}")))
            .andExpect(jsonPath("$._links.collection.templated", equalTo(true)))
            .andExpect(jsonPath("$._links.createCollection.href", endsWith("collections")))

            .andExpect(jsonPath("$._links.videos").doesNotExist())
            .andExpect(jsonPath("$._links.adminSearch").doesNotExist())
            .andExpect(jsonPath("$._links.contentPartner").doesNotExist())
            .andExpect(jsonPath("$._links.contentPartners").doesNotExist())
    }

    @Test
    fun `when can view restricted videos GET returns admin search`() {
        mockMvc.perform(get("/v1").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.adminSearch.href", containsString("/videos/search")))
    }

    @Test
    fun `link to any users collection included when user has access rights`() {
        mockMvc.perform(get("/v1").asSubjectClassifier())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.collectionsByOwner.href", containsString("/collections?")))
    }

    @Test
    fun `GET links uses proto headers`() {
        mockMvc.perform(get("/v1").header("x-forwarded-proto", "https"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.video.href", startsWith("https")))
    }

    @Test
    fun `optional parameters are not required by link template`() {
        val response = mockMvc.perform(get("/v1").asTeacher()).andReturn().response.contentAsString

        val searchUrlTemplate: String = JsonPath.parse(response).read("$._links.search.href")
        val searchUrl =
            UriTemplate(searchUrlTemplate).expand(mapOf(("query" to "phrase"), ("size" to 1), ("page" to 1)))

        assertThat(searchUrl.toASCIIString()).endsWith("/videos?query=phrase&size=1&page=1")
    }

    @Test
    fun `can view content partners link with correct role`() {
        mockMvc.perform(get("/v1").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.contentPartners.href", containsString("/content-partners")))
            .andExpect(jsonPath("$._links.contentPartner.href", containsString("/content-partners/{id}")))
            .andExpect(jsonPath("$._links.contentPartner.templated", equalTo(true)))
    }

}

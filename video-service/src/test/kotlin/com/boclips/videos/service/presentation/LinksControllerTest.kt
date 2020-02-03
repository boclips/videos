package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asApiUser
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class LinksControllerTest : AbstractSpringIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `as a not authenticated`() {
        mockMvc.perform(get("/v1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.video.href", containsString("/videos/{id}")))
            .andExpect(jsonPath("$._links.video.templated", equalTo(true)))
            .andExpect(
                jsonPath(
                    "$._links.publicCollections.href",
                    endsWith("collections?projection=list&public=true&page=0&size=30")
                )
            )
            .andExpect(jsonPath("$._links.subjects.href", endsWith("/subjects")))
            .andExpect(jsonPath("$._links.createPlaybackEvent").doesNotExist())
            .andExpect(jsonPath("$._links.createPlaybackEvents").doesNotExist())
            .andExpect(jsonPath("$._links.createVideoVisitedEvent").doesNotExist())
            .andExpect(jsonPath("$._links.adminSearch").doesNotExist())
            .andExpect(jsonPath("$._links.adminVideoSearch").doesNotExist())
            .andExpect(jsonPath("$._links.adminCollectionSearch").doesNotExist())
            .andExpect(jsonPath("$._links.distributionMethods").doesNotExist())
            .andExpect(jsonPath("$._links.searchVideos").doesNotExist())
            .andExpect(jsonPath("$._links.searchPublicCollections").doesNotExist())
            .andExpect(jsonPath("$._links.searchCollections").doesNotExist())
            .andExpect(jsonPath("$._links.videos").doesNotExist())
            .andExpect(jsonPath("$._links.myCollections").doesNotExist())
            .andExpect(jsonPath("$._links.collectionsByOwner").doesNotExist())
            .andExpect(jsonPath("$._links.bookmarkedCollections").doesNotExist())
            .andExpect(jsonPath("$._links.collection").doesNotExist())
            .andExpect(jsonPath("$._links.legalRestrictions").doesNotExist())
            .andExpect(jsonPath("$._links.disciplines").doesNotExist())
            .andExpect(jsonPath("$._links.tags").doesNotExist())
            .andExpect(jsonPath("$._links.videoTypes").doesNotExist())
            .andExpect(jsonPath("$._links.contentCategories").doesNotExist())
    }

    @Test
    fun `as an authenticated user`() {
        val userId = "teacher@teacher.com"
        mockMvc.perform(get("/v1").asTeacher(userId))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.searchVideos.href", containsString("/videos")))
            .andExpect(
                jsonPath(
                    "$._links.searchVideos.href",
                    containsString("{?query,sort_by,duration_min,duration_max,released_date_from,released_date_to,source,age_range_min,age_range_max,size,page,subject,subjects_set_manually,promoted,content_partner,type,is_classroom}")
                )
            )
            .andExpect(jsonPath("$._links.searchVideos.templated", equalTo(true)))
            .andExpect(jsonPath("$._links.subjects.href", endsWith("/subjects")))
            .andExpect(
                jsonPath(
                    "$._links.searchPublicCollections.href",
                    endsWith("collections?public=true{&query,subject,projection,page,size,age_range_min,age_range_max}")
                )
            )
            .andExpect(
                jsonPath(
                    "$._links.searchCollections.href",
                    endsWith("collections{?query,subject,public,projection,page,size,age_range_min,age_range_max}")
                )
            )
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
            .andExpect(jsonPath("$._links.createPlaybackEvent").doesNotExist())
            .andExpect(jsonPath("$._links.createPlaybackEvents.href", endsWith("events/playback/batch")))

            .andExpect(jsonPath("$._links.collectionsByOwner").doesNotExist())
            .andExpect(jsonPath("$._links.collection.href", endsWith("collections/{id}")))
            .andExpect(jsonPath("$._links.collection.templated", equalTo(true)))
            .andExpect(jsonPath("$._links.createCollection.href", endsWith("collections")))
            .andExpect(jsonPath("$._links.disciplines.href", endsWith("disciplines")))
            .andExpect(jsonPath("$._links.subjects.href", endsWith("subjects")))
            .andExpect(jsonPath("$._links.tags.href", endsWith("tags")))

            .andExpect(jsonPath("$._links.videos").doesNotExist())
            .andExpect(jsonPath("$._links.adminSearch").doesNotExist())
            .andExpect(jsonPath("$._links.adminVideoSearch").doesNotExist())
            .andExpect(jsonPath("$._links.adminCollectionSearch").doesNotExist())
            .andExpect(jsonPath("$._links.distributionMethods").doesNotExist())
            .andExpect(jsonPath("$._links.contentPartner").doesNotExist())
            .andExpect(jsonPath("$._links.contentPartners").doesNotExist())
            .andExpect(jsonPath("$._links.legalRestrictions").doesNotExist())
    }

    @Test
    fun `return videoTypes link when called as API user`() {
        mockMvc.perform(get("/v1").asApiUser()).andExpect(status().isOk)
            .andExpect(jsonPath("$._links.videoTypes.href", endsWith("/video-types")))
    }

    @Test
    fun `return contentCategories link when called as API user`() {
        mockMvc.perform(get("/v1").asApiUser()).andExpect(status().isOk)
            .andExpect(jsonPath("$._links.contentCategories.href", endsWith("/content-categories")))
    }

    @Test
    fun `as Boclips employee see admin search`() {
        mockMvc.perform(get("/v1").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.adminSearch.href", containsString("/videos/search")))
            .andExpect(jsonPath("$._links.adminVideoSearch.href", containsString("/videos/search")))
            .andExpect(
                jsonPath(
                    "$._links.adminCollectionSearch.href",
                    containsString("/collections{?query,subject,projection,page,size}")
                )
            )
    }

    @Test
    fun `as Boclips employee see distribution methods`() {
        mockMvc.perform(get("/v1").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.distributionMethods.href", endsWith("/distribution-methods")))
    }

    @Test
    fun `as Boclips employee I can view content partners link with correct role`() {
        mockMvc.perform(get("/v1").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.contentPartners.href", containsString("/content-partners")))
            .andExpect(jsonPath("$._links.contentPartner.href", containsString("/content-partners/{id}")))
            .andExpect(jsonPath("$._links.contentPartner.templated", equalTo(true)))
    }

    @Test
    fun `as subject classifier I can link to any users collection included when user has access rights`() {
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

        val searchUrlTemplate: String = JsonPath.parse(response).read("$._links.searchVideos.href")
        val searchUrl =
            UriTemplate(searchUrlTemplate).expand(mapOf(("query" to "phrase"), ("size" to 1), ("page" to 1)))

        assertThat(searchUrl.toASCIIString()).endsWith("/videos?query=phrase&size=1&page=1")
    }
}

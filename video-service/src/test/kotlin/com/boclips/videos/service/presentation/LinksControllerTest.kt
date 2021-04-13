package com.boclips.videos.service.presentation

import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.testsupport.*
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
            .andExpect(jsonPath("$._links.video.href", endsWith("videos/{id}{?referer,shareCode}")))
            .andExpect(jsonPath("$._links.video.templated", equalTo(true)))
            .andExpect(
                jsonPath(
                    "$._links.promotedCollections.href",
                    endsWith("collections?projection=list&promoted=true&discoverable=true&page=0&size=30")
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
            .andExpect(jsonPath("$._links.collection").exists())
            .andExpect(jsonPath("$._links.legalRestrictions").doesNotExist())
            .andExpect(jsonPath("$._links.disciplines").doesNotExist())
            .andExpect(jsonPath("$._links.tags").doesNotExist())
            .andExpect(jsonPath("$._links.videoTypes").doesNotExist())
            .andExpect(jsonPath("$._links.attachmentTypes").doesNotExist())
            .andExpect(jsonPath("$._links.contentCategories").doesNotExist())
            .andExpect(jsonPath("$._links.ageRanges").doesNotExist())
            .andExpect(jsonPath("$._links.marketingStatuses").doesNotExist())
            .andExpect(jsonPath("$._links.contracts").doesNotExist())
            .andExpect(jsonPath("$._links.getCaptions").doesNotExist())
            .andExpect(jsonPath("$._links.channels").doesNotExist())
            .andExpect(jsonPath("$._links.channel").doesNotExist())
            .andExpect(jsonPath("$._links.searchQueryCompletionsSuggestedEvent").doesNotExist())
            .andExpect(jsonPath("$._links.getVideoPrice").doesNotExist())
            .andExpect(jsonPath("$._links.taxonomies").doesNotExist())
    }

    @Test
    fun `as an authenticated Teacher user`() {
        val userId = "teacher@teacher.com"
        mockMvc.perform(get("/v1").asTeacher(userId))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.searchVideos.href", containsString("/videos")))
            .andExpect(jsonPath("$._links.searchVideos.templated", equalTo(true)))
            .andExpect(
                jsonPath(
                    "$._links.searchVideos.href",
                    containsString("{?query,id,sort_by,duration,duration_facets,duration_min,duration_max,released_date_from,released_date_to,source,age_range_min,age_range_max,age_range,age_range_facets,size,page,subject,subjects_set_manually,promoted,content_partner,type,channel,resource_types,resource_type_facets,include_channel_facets,prices}")
                )
            )
            .andExpect(
                jsonPath(
                    "$._links.suggestions.href",
                    endsWith("suggestions?query={query}")
                )
            )
            .andExpect(jsonPath("$._links.subjects.href", endsWith("/subjects")))
            .andExpect(
                jsonPath(
                    "$._links.searchCollections.href",
                    endsWith("collections{?query,subject,discoverable,projection,page,size,age_range_min,age_range_max,age_range,resource_types,sort_by}")
                )
            )
            .andExpect(
                jsonPath(
                    "$._links.discoverCollections.href",
                    endsWith("collections?projection=list&discoverable=true&page=0&size=30{&query,subject,sort_by}")
                )
            )
            .andExpect(
                jsonPath(
                    "$._links.promotedCollections.href",
                    endsWith("collections?projection=list&promoted=true&discoverable=true&page=0&size=30")
                )
            )
            .andExpect(
                jsonPath(
                    "$._links.myCollections.href",
                    endsWith("/v1/users/teacher@teacher.com/collections?bookmarked=false{&projection,page,size,sort_by}")
                )
            )
            .andExpect(
                jsonPath(
                    "$._links.mySavedCollections.href",
                    endsWith("/v1/users/teacher@teacher.com/collections{?projection,page,size,sort_by}")
                )
            )
            .andExpect(
                jsonPath(
                    "$._links.ageRanges.href",
                    endsWith("/v1/age-ranges")
                )
            )
            .andExpect(jsonPath("$._links.createPlaybackEvent").doesNotExist())
            .andExpect(jsonPath("$._links.createPlaybackEvents.href", endsWith("events/playback/batch")))
            .andExpect(jsonPath("$._links.createSearchQueryCompletionsSuggestedEvent.href", endsWith("events/suggested-search-completions")))

            .andExpect(jsonPath("$._links.collectionsByOwner").doesNotExist())
            .andExpect(jsonPath("$._links.collection.href", endsWith("collections/{id}{?referer,shareCode}")))
            .andExpect(jsonPath("$._links.collection.templated", equalTo(true)))
            .andExpect(jsonPath("$._links.createCollection.href", endsWith("collections")))
            .andExpect(jsonPath("$._links.disciplines.href", endsWith("disciplines")))
            .andExpect(jsonPath("$._links.subjects.href", endsWith("subjects")))
            .andExpect(jsonPath("$._links.tags.href", endsWith("tags")))

            .andExpect(jsonPath("$._links.videos").doesNotExist())
            .andExpect(jsonPath("$._links.getCaptions").doesNotExist())
            .andExpect(jsonPath("$._links.adminCollectionSearch").doesNotExist())
            .andExpect(jsonPath("$._links.distributionMethods").doesNotExist())
            .andExpect(jsonPath("$._links.contentPartner").doesNotExist())
            .andExpect(jsonPath("$._links.contentPartners").doesNotExist())
            .andExpect(jsonPath("$._links.channels").doesNotExist())
            .andExpect(jsonPath("$._links.channel").doesNotExist())
            .andExpect(jsonPath("$._links.contract").doesNotExist())
            .andExpect(jsonPath("$._links.contracts").doesNotExist())
            .andExpect(jsonPath("$._links.contentPartnersSignedUploadLink").doesNotExist())
            .andExpect(jsonPath("$._links.createContractsSignedUploadLink").doesNotExist())
            .andExpect(jsonPath("$._links.legalRestrictions").doesNotExist())
            .andExpect(jsonPath("$._links.attachmentTypes").doesNotExist())
            .andExpect(jsonPath("$._links.getVideoPrice").doesNotExist())
            .andExpect(jsonPath("$._links.taxonomies").doesNotExist())
    }

    @Test
    fun `return age ranges link when called as Boclips employee api user`() {
        mockMvc.perform(get("/v1").asBoclipsEmployee()).andExpect(status().isOk)
            .andExpect(jsonPath("$._links.ageRanges.href", endsWith("/age-ranges")))
    }

    @Test
    fun `as Boclips employee see admin links`() {
        mockMvc.perform(get("/v1").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.distributionMethods.href", endsWith("/distribution-methods")))
            .andExpect(jsonPath("$._links.channels.href", containsString("/channels")))
            .andExpect(
                jsonPath(
                    "$._links.channelsSignedUploadLink.href",
                    containsString("/channels/signed-upload-link")
                )
            )
            .andExpect(jsonPath("$._links.channel.href", containsString("/channels/{id}")))
            .andExpect(jsonPath("$._links.channel.templated", equalTo(true)))
            .andExpect(
                jsonPath(
                    "$._links.contract.href",
                    containsString("/contracts/{id}")
                )
            )
            .andExpect(jsonPath("$._links.contract.templated", equalTo(true)))
            .andExpect(
                jsonPath(
                    "$._links.createContractsSignedUploadLink.href",
                    containsString(
                        "/contracts/signed-upload-link"
                    )
                )
            )
            .andExpect(jsonPath("$._links.marketingStatuses.href", endsWith("/marketing-statuses")))
            .andExpect(
                jsonPath(
                    "$._links.contracts.href",
                    endsWith("/contracts{?size,page,projection}")
                )
            )
            .andExpect(jsonPath("$._links.contracts.templated", equalTo(true)))
            .andExpect(jsonPath("$._links.contractLegalRestrictions.href", endsWith("/contract-legal-restrictions")))
            .andExpect(jsonPath("$._links.contractLegalRestrictions.templated", equalTo(false)))
            .andExpect(jsonPath("$._links.getCaptions.href", endsWith("/captions")))
            .andExpect(jsonPath("$._links.getCaptions.templated", equalTo(true)))
            .andExpect(jsonPath("$._links.contentWarnings.href", endsWith("/content-warnings")))
            .andExpect(jsonPath("$._links.contentWarnings.templated", equalTo(false)))
            .andExpect(jsonPath("$._links.attachmentTypes.href", endsWith("/attachment-types")))
            .andExpect(jsonPath("$._links.taxonomies.href", endsWith("/taxonomies")))
            .andExpect(jsonPath("$._links.categories.href", endsWith("/categories")))
    }

    @Test
    fun `return getVideoPrice link when called as Boclips service user`() {
        mockMvc.perform(get("/v1").asUserWithRoles(UserRoles.BOCLIPS_SERVICE)).andExpect(status().isOk)
            .andExpect(jsonPath("$._links.getVideoPrice.href", endsWith("{?userId}")))
    }

    @Test
    fun `return contentCategories link when called as API user`() {
        mockMvc.perform(get("/v1").asApiUser()).andExpect(status().isOk)
            .andExpect(jsonPath("$._links.contentCategories.href", endsWith("/content-categories")))
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
            UriTemplate.of(searchUrlTemplate).expand(mapOf(("query" to "phrase"), ("size" to 1), ("page" to 1)))

        assertThat(searchUrl.toASCIIString()).endsWith("/videos?query=phrase&size=1&page=1")
    }
}

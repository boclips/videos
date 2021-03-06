package com.boclips.videos.service.testsupport

import com.boclips.users.api.factories.AccessRulesResourceFactory
import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.UriTemplate
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.util.UUID

abstract class AbstractCollectionsControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @BeforeEach
    fun cleanupContracts() {
        usersClient.clear()
    }

    fun createCollection(
        title: String = "a collection name",
        description: String = "a description",
        discoverable: Boolean = false,
        owner: String = "teacher@gmail.com"
    ): String =
        mockMvc.perform(
            post("/v1/collections").contentType(MediaType.APPLICATION_JSON).content(
                """{"title": "$title", "description": "$description", "discoverable": $discoverable}"""
            ).asTeacher(owner)
        )
            .andExpect(status().isCreated)
            .andReturn().response.getHeader("Location")!!.substringAfterLast("/")

    fun addVideo(collectionId: String, videoId: String) {
        mockMvc.perform(put(addVideoLink(collectionId, videoId)).asTeacher())
            .andExpect(status().isNoContent)
    }

    fun addVideoLink(collectionId: String, videoId: String): URI {
        return getCollection(collectionId)
            .andReturn()
            .extractVideoLink("addVideo", videoId)
    }

    fun getCollection(collectionId: String, user: String = "teacher@gmail.com"): ResultActions {
        return mockMvc.perform(get("/v1/collections/$collectionId").asTeacher(user))
            .andExpect(status().isOk)
    }

    fun updateCollectionToBeDiscoverable(collectionId: String) {
        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).content(
                """{"discoverable": "true"}"""
            ).asTeacher()
        )
            .andExpect(status().isNoContent)
    }

    fun updateCollectionToBePromoted(collectionId: String) {
        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).content(
                """{"promoted": "true"}"""
            ).asBoclipsEmployee()
        )
            .andExpect(status().isNoContent)
    }

    fun updateCollectionAgeRange(
        collectionId: String,
        ageRangeMin: Int? = null,
        ageRangeMax: Int? = null
    ) {
        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).content(
                """{"ageRange": {"min": $ageRangeMin, "max": $ageRangeMax}}"""
            ).asTeacher()
        )
            .andExpect(status().isNoContent)
    }

    fun updateCollectionAttachment(
        collectionId: String,
        attachmentType: String,
        attachmentDescription: String,
        attachmentURL: String
    ) {
        mockMvc.perform(
            patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).content(
                """{"attachment": {
                    |"description" : "$attachmentDescription",
                    |"linkToResource" : "$attachmentURL",
                    |"type": "$attachmentType"}}""".trimMargin()
            ).asTeacher()
        )
            .andExpect(status().isNoContent)
    }

    fun selfLink(collectionId: String): URI {
        return getCollection(collectionId)
            .andReturn()
            .extractLink("self")
    }

    fun createCollectionWithTitle(
        title: String,
        email: String = "teacher@gmail.com",
        discoverable: Boolean = false
    ): String {
        return collectionRepository.create(
            CreateCollectionCommand(
                owner = UserId(email),
                title = title,
                createdByBoclips = false,
                discoverable = discoverable
            )
        ).id.value
    }

    fun createIncludedCollectionsAccessRules(userId: String, vararg contractedCollectionIds: String) {
        usersClient.addAccessRules(
            userId = userId,
            accessRulesResource = AccessRulesResourceFactory.sample(
                AccessRuleResource.IncludedCollections(
                    name = UUID.randomUUID().toString(),
                    collectionIds = contractedCollectionIds.toList()
                )
            )
        )
    }

    fun MvcResult.extractLink(relName: String): URI {
        return URI(JsonPath.parse(response.contentAsString).read<String>("$._links.$relName.href"))
    }

    fun MvcResult.extractVideoLink(relName: String, videoId: String): URI {
        val templateString = JsonPath.parse(response.contentAsString).read<String>("$._links.$relName.href")
        return UriTemplate.of(templateString).expand(mapOf(("video_id" to videoId)))
    }

    protected fun bookmarkCollection(collectionId: String, user: String) {
        mockMvc.perform(patch(bookmarkLink(collectionId, user)).contentType(MediaType.APPLICATION_JSON).asTeacher(user))
            .andExpect(status().isOk)
    }

    private fun bookmarkLink(collectionId: String, user: String): URI {
        return getCollection(collectionId, user)
            .andReturn()
            .extractLink("bookmark")
    }
}

package com.boclips.videos.service.testsupport

import com.boclips.users.client.model.contract.SelectedCollectionsContract
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.UriTemplate
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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
        userServiceClient.clearContracts()
    }

    fun createCollection(
        title: String = "a collection name",
        description: String = "a description",
        public: Boolean = false
    ) =
        mockMvc.perform(
            post("/v1/collections").contentType(MediaType.APPLICATION_JSON).content(
                """{"title": "$title", "description": "$description", "public": $public}"""
            ).asTeacher()
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

    fun updateCollectionToBePublic(collectionId: String) {
        mockMvc.perform(
            MockMvcRequestBuilders.patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).content(
                """{"public": "true"}"""
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
            MockMvcRequestBuilders.patch(selfLink(collectionId)).contentType(MediaType.APPLICATION_JSON).content(
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
        isPublic: Boolean = false
    ): String {
        return collectionRepository.create(
            CreateCollectionCommand(
                owner = UserId(email),
                title = title,
                createdByBoclips = false,
                public = isPublic
            )
        ).id.value
    }

    fun createSelectedCollectionsContract(vararg contractedCollectionIds: String) {
        userServiceClient.addContract(SelectedCollectionsContract().apply {
            name = UUID.randomUUID().toString()
            collectionIds = contractedCollectionIds.toList()
        })
    }

    fun MvcResult.extractLink(relName: String): URI {
        return URI(JsonPath.parse(response.contentAsString).read<String>("$._links.$relName.href"))
    }

    fun MvcResult.extractVideoLink(relName: String, videoId: String): URI {
        val templateString = JsonPath.parse(response.contentAsString).read<String>("$._links.$relName.href")
        return UriTemplate(templateString).expand(mapOf(("video_id" to videoId)))
    }
}

package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.testsupport.AbstractCollectionsControllerIntegrationTest
import com.boclips.videos.service.testsupport.MvcMatchers.halJson
import com.boclips.videos.service.testsupport.UserFactory
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CollectionsControllerSubCollectionsIntegrationTest : AbstractCollectionsControllerIntegrationTest() {
    @Test
    fun `collections can contain units`() {
        val collection = collectionRepository.create(
            CreateCollectionCommand(
                title = "some collection",
                owner = UserId("some-user"),
                discoverable = false,
                description = "Some description",
                createdByBoclips = false,
                subjects = emptySet()
            )
        )

        val unit = collectionRepository.create(
            CreateCollectionCommand(
                title = "some unit",
                owner = UserId("some-user"),
                discoverable = false,
                description = "Some description",
                createdByBoclips = false,
                subjects = emptySet()
            )
        )

        collectionRepository.update(
            CollectionUpdateCommand.AddCollectionToCollection(
                collectionId = collection.id,
                subCollectionId = unit.id,
                user = UserFactory.sample()
            )
        )

        mockMvc.perform(get("/v1/collections/${collection.id.value}").asTeacher())
            .andExpect(status().isOk)
            .andExpect(halJson())
            .andExpect(jsonPath("$.id", Matchers.not(Matchers.emptyString())))
            .andExpect(jsonPath("$.title", Matchers.equalTo("some collection")))
            .andExpect(jsonPath("$.subCollections[*]", hasSize<Any>(1)))
            .andExpect(jsonPath("$.subCollections[0].title", Matchers.equalTo("some unit")))
    }
}

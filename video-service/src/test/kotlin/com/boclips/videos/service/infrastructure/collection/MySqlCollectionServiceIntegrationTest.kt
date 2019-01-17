package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.AddVideoToCollection
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired


class MySqlCollectionServiceIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionService: MySqlCollectionService

    @Test
    fun `get throws when collection not found`() {
        assertThrows<CollectionNotFoundException> { collectionService.getById(CollectionId("idthatdoesnotexist")) }
    }

    @Test
    fun `create`() {
        val collection = collectionService.create(owner = "user@gmail.com")
        assertThat(collection.id.value).isNotBlank()
        assertThat(collection.owner).isEqualTo("user@gmail.com")
        assertThat(collection.title).isBlank()
        assertThat(collection.videos).isEmpty()
    }

    @Test
    fun `retrieve by id`() {
        val collectionId = collectionService.create(owner = "user@gmail.com").id
        val collection = collectionService.getById(collectionId)
        assertThat(collection.id.value).isNotBlank()
        assertThat(collection.title).isBlank()
        assertThat(collection.videos).isEmpty()
    }

    @Test
    fun `retrieve by owner`() {
        assertThat(collectionService.getByOwner(owner = "user@gmail.com")).hasSize(0)

        collectionService.create(owner = "user@gmail.com")
        collectionService.create(owner = "user@gmail.com")

        assertThat(collectionService.getByOwner(owner = "user@gmail.com")).hasSize(2)
    }

    @Test
    fun `add a video to collection`() {
        saveVideo(videoId = 10)

        val collectionId = collectionService.create(owner = "user@gmail.com").id

        collectionService.update(collectionId, AddVideoToCollection(AssetId("10")))

        val collection = collectionService.getById(collectionId)

        assertThat(collection.videos).isNotEmpty
    }
}
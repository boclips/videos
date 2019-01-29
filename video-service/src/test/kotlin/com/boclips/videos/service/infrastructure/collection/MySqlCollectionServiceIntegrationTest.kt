package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.AddVideoToCollection
import com.boclips.videos.service.domain.service.RemoveVideoFromCollection
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
        val collection = createCollection(owner = UserId(value = "user@gmail.com"))

        assertThat(collection.id.value).isNotBlank()
        assertThat(collection.owner).isEqualTo(UserId("user@gmail.com"))
        assertThat(collection.title).isBlank()
        assertThat(collection.videos).isEmpty()
    }

    @Test
    fun `retrieve by id`() {
        val collectionId = createCollection().id

        val collection = collectionService.getById(collectionId)

        assertThat(collection.id.value).isNotBlank()
        assertThat(collection.title).isBlank()
        assertThat(collection.videos).isEmpty()
    }

    @Test
    fun `retrieve by owner`() {
        assertThat(collectionService.getByOwner(owner = UserId(value = "user@gmail.com"))).hasSize(0)

        createCollection(owner = UserId(value = "user@gmail.com"))
        createCollection(owner = UserId(value = "user@gmail.com"))
        createCollection(owner = UserId(value = "another-user@gmail.com"))

        assertThat(collectionService.getByOwner(owner = UserId(value = "user@gmail.com"))).hasSize(2)
    }

    @Test
    fun `retrieve by owner when owner does not exist`() {
        assertThat(collectionService.getByOwner(owner = UserId(value = "tod"))).hasSize(0)
    }

    @Test
    fun `add a video to collection`() {
        val videoId = saveVideo()

        val collectionId = createCollection().id

        collectionService.update(collectionId, AddVideoToCollection(videoId))

        val collection = collectionService.getById(collectionId)

        assertThat(collection.videos).isNotEmpty
    }

    @Test
    fun `add a video to collection ignored when video already there`() {
        val videoId = saveVideo()

        val collectionId = createCollection().id

        collectionService.update(collectionId, AddVideoToCollection(videoId))
        collectionService.update(collectionId, AddVideoToCollection(videoId))

        val collection = collectionService.getById(collectionId)

        assertThat(collection.videos).hasSize(1)
    }

    @Test
    fun `remove a video from collection`() {
        val videoId = saveVideo()

        val collectionId = createCollection().id

        collectionService.update(collectionId, AddVideoToCollection(videoId))
        collectionService.update(collectionId, RemoveVideoFromCollection(videoId))

        val collection = collectionService.getById(collectionId)

        assertThat(collection.videos).isEmpty()
    }

    @Test
    fun `remove a video from collection ignored when video isn't present`() {
        val collectionId = createCollection().id

        collectionService.update(collectionId, RemoveVideoFromCollection(AssetId("10")))

        val collection = collectionService.getById(collectionId)

        assertThat(collection.videos).isEmpty()
    }

    private fun createCollection(owner: UserId =  UserId(value = "user@gmail.com")) = collectionService.create(owner = owner)
}
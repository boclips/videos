package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.service.collection.AddVideoToCollectionCommand
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.RemoveVideoFromCollectionCommand
import com.boclips.videos.service.domain.service.collection.RenameCollectionCommand
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

class MongoCollectionServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionService: CollectionService

    @Test
    fun `can create and add videos to a collection`() {
        val videoAsset1 = saveVideo()
        val videoAsset2 = saveVideo()

        val collection = collectionService.create(
            owner = UserId(value = "user1"),
            title = "Collection vs Playlist"
        )

        collectionService.update(
            collection.id,
            AddVideoToCollectionCommand(videoAsset1)
        )
        collectionService.update(
            collection.id,
            AddVideoToCollectionCommand(videoAsset2)
        )
        collectionService.update(
            collection.id,
            RemoveVideoFromCollectionCommand(videoAsset1)
        )

        val updatedCollection = collectionService.getById(collection.id)!!

        assertThat(updatedCollection.owner).isEqualTo(UserId(value = "user1"))
        assertThat(updatedCollection.videos).hasSize(1)
        assertThat(updatedCollection.videos.map { it.asset.assetId }).contains(videoAsset2)
        assertThat(updatedCollection.title).isEqualTo("Collection vs Playlist")
    }

    @Test
    fun `can create and then rename a collection`() {
        val collection = collectionService.create(
                owner = UserId(value = "user1"),
                title = "Starting Title"
        )

        collectionService.update(collection.id, RenameCollectionCommand("New Title"))

        val updatedCollection = collectionService.getById(collection.id)!!

        assertThat(updatedCollection.title).isEqualTo("New Title")
    }

    @Test
    fun `can delete a collection`() {
        val collection = collectionService.create(
                owner = UserId(value = "user1"),
                title = "Starting Title"
        )

        collectionService.delete(collection.id)

        val deletedCollection = collectionService.getById(collection.id)

        assertThat(deletedCollection).isNull()
    }

    @Test
    fun `updatedAt timestamp on modifying changes`() {
        val videoAsset1 = saveVideo()

        val moment = Instant.now()
        val collectionV1 = collectionService.create(owner = UserId(value = "user1"), title = "My Videos")

        assertThat(collectionV1.updatedAt).isBetween(moment.minusSeconds(10), moment.plusSeconds(10))

        collectionService.update(
                collectionV1.id,
                AddVideoToCollectionCommand(videoAsset1)
        )

        val collectionV2 = collectionService.getById(collectionV1.id)!!

        assertThat(collectionV2.updatedAt).isAfter(collectionV1.updatedAt)

        collectionService.update(
                collectionV2.id,
                RemoveVideoFromCollectionCommand(videoAsset1)
        )

        val collectionV3 = collectionService.getById(collectionV1.id)!!

        assertThat(collectionV3.updatedAt).isAfter(collectionV2.updatedAt)
    }

    @Test
    fun `can retrieve collection of user`() {
        val videoInCollection = saveVideo()
        val collection = collectionService.create(
            owner = UserId(value = "user1"),
            title = ""
        )
        collectionService.update(
            collection.id,
            AddVideoToCollectionCommand(videoInCollection)
        )

        val userCollection = collectionService.getByOwner(UserId(value = "user1"))

        assertThat(userCollection.size).isEqualTo(1)
    }
}

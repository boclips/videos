package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.service.collection.AddVideoToCollection
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.RemoveVideoFromCollection
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MongoCollectionServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionService: CollectionService

    @Test
    fun `can create and update a collection`() {
        val videoAsset1 = saveVideo()
        val videoAsset2 = saveVideo()

        val collection = collectionService.create(
            owner = UserId(value = "user1"),
            title = "Collection vs Playlist"
        )

        collectionService.update(
            collection.id,
            AddVideoToCollection(videoAsset1)
        )
        collectionService.update(
            collection.id,
            AddVideoToCollection(videoAsset2)
        )
        collectionService.update(
            collection.id,
            RemoveVideoFromCollection(videoAsset1)
        )

        val updatedCollection = collectionService.getById(collection.id)

        assertThat(updatedCollection!!.owner).isEqualTo(UserId(value = "user1"))
        assertThat(updatedCollection!!.videos).hasSize(1)
        assertThat(updatedCollection.title).isEqualTo("Collection vs Playlist")
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
            AddVideoToCollection(videoInCollection)
        )

        val userCollection = collectionService.getByOwner(UserId(value = "user1"))

        assertThat(userCollection.size).isEqualTo(1)
    }
}

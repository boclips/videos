package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.PageRequest
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.AddVideoToCollectionCommand
import com.boclips.videos.service.domain.service.collection.ChangeVisibilityCommand
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.RemoveVideoFromCollectionCommand
import com.boclips.videos.service.domain.service.collection.RenameCollectionCommand
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.Date

class MongoCollectionServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionService: CollectionService

    @Test
    fun `can create and add videos to a collection`() {
        val videoAsset1 = saveVideo()
        val videoAsset2 = saveVideo()

        val collection = collectionService.create(
            owner = UserId(value = "user1"),
            title = "Collection vs Playlist",
            createdByBoclips = false
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
        assertThat(updatedCollection.videos).contains(videoAsset2)
        assertThat(updatedCollection.title).isEqualTo("Collection vs Playlist")
    }

    @Test
    fun `can create and then rename a collection`() {
        val collection = collectionService.create(
            owner = UserId(value = "user1"),
            title = "Starting Title",
            createdByBoclips = false
        )

        collectionService.update(collection.id, RenameCollectionCommand("New Title"))

        val updatedCollection = collectionService.getById(collection.id)!!

        assertThat(updatedCollection.title).isEqualTo("New Title")
    }

    @Test
    fun `can create and mark a collection as public`() {
        val collection = collectionService.create(
            owner = UserId(value = "user1"),
            title = "Starting Title",
            createdByBoclips = false
        )
        assertThat(collection.isPublic).isEqualTo(false)

        collectionService.update(collection.id, ChangeVisibilityCommand(isPublic = true))

        val updatedCollection = collectionService.getById(collection.id)!!

        assertThat(updatedCollection.isPublic).isEqualTo(true)
    }

    @Test
    fun `can delete a collection`() {
        val collection = collectionService.create(
            owner = UserId(value = "user1"),
            title = "Starting Title",
            createdByBoclips = false
        )

        collectionService.delete(collection.id)

        val deletedCollection = collectionService.getById(collection.id)

        assertThat(deletedCollection).isNull()
    }

    @Test
    fun `updatedAt timestamp on modifying changes`() {
        val videoAsset1 = saveVideo()

        val moment = Instant.now()
        val collectionV1 = collectionService.create(
            owner = UserId(value = "user1"),
            title = "My Videos",
            createdByBoclips = false
        )

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
            title = "",
            createdByBoclips = false
        )
        collectionService.update(
            collection.id,
            AddVideoToCollectionCommand(videoInCollection)
        )

        val userCollection = collectionService.getByOwner(UserId(value = "user1"))

        assertThat(userCollection.size).isEqualTo(1)
    }

    @Test
    fun `can retrieve public collections`() {
        val publicCollection = collectionService.create(
            owner = UserId(value = "user1"),
            title = "Starting Title",
            createdByBoclips = false
        )

        val publicCollection2 = collectionService.create(
            owner = UserId(value = "user2"),
            title = "Starting Title",
            createdByBoclips = false
        )

        val privateCollection = collectionService.create(
            owner = UserId(value = "user1"),
            title = "Starting Title",
            createdByBoclips = false
        )

        collectionService.update(publicCollection.id, ChangeVisibilityCommand(true))
        collectionService.update(publicCollection2.id, ChangeVisibilityCommand(true))

        val publicCollections = collectionService.getPublic(PageRequest(0, 10))

        assertThat(publicCollections.elements).hasSize(2)
        assertThat(publicCollections.elements.map { it.id }).contains(publicCollection.id, publicCollection2.id)

        assertThat(publicCollections.elements).doesNotContain(privateCollection)
    }

    @Test
    fun `can retrieve pages of public collections ordered by last edited`() {
        val publicCollection1 = collectionService.create(
            owner = UserId(value = "user1"),
            title = "Starting Title",
            createdByBoclips = false
        )

        val publicCollection2 = collectionService.create(
            owner = UserId(value = "user2"),
            title = "Starting Title",
            createdByBoclips = false
        )

        collectionService.update(publicCollection1.id, ChangeVisibilityCommand(true))

        Thread.sleep(500)

        collectionService.update(publicCollection2.id, ChangeVisibilityCommand(true))

        val firstPage = collectionService.getPublic(PageRequest(0, 1))
        assertThat(firstPage.pageInfo.hasMoreElements).isTrue()
        assertThat(firstPage.elements).hasSize(1)
        assertThat(firstPage.elements.map { it.id }).contains(publicCollection2.id)

        val lastPage = collectionService.getPublic(PageRequest(1, 1))
        assertThat(lastPage.pageInfo.hasMoreElements).isFalse()
        assertThat(lastPage.elements).hasSize(1)
        assertThat(lastPage.elements.map { it.id }).contains(publicCollection1.id)
    }

    @Nested
    inner class MigrationTests {
        @Test
        fun `can retrieve legacy documents ands marks collections as private when they not contain isPublic property`() {
            mongoClient
                .getDatabase(DATABASE_NAME)
                .getCollection(MongoCollectionService.collectionName)
                .insertOne(
                    Document()
                        .append("_id", ObjectId("5c55697860fef77aa4af323a"))
                        .append("title", "My Videos")
                        .append("owner", "a4efeee2-0166-4371-ba72-0fa5a13c9aca")
                        .append("updatedAt", Date())
                        .append("videos", emptyList<AssetId>())
                )

            val collection = collectionService.getById(CollectionId(value = "5c55697860fef77aa4af323a"))!!

            assertThat(collection.isPublic).isEqualTo(false)
        }
    }
}

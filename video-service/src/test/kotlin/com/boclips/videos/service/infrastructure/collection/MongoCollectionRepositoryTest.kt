package com.boclips.videos.service.infrastructure.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.collection.CollectionsUpdateCommand
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.Date

class MongoCollectionRepositoryTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Nested
    inner class Find {
        @Test
        fun `find by subject`() {
            val collection = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "Collection title",
                createdByBoclips = false,
                public = false
            )

            val subject = TestFactories.createSubject()

            collectionRepository.update(
                collection.id,
                CollectionUpdateCommand.ReplaceSubjects(setOf(subject))
            )

            val findAllBySubject = collectionRepository.findAllBySubject(subject.id)

            assertThat(findAllBySubject).hasSize(1)
        }
    }

    @Nested
    inner class CreateAndUpdateOne {
        @Test
        fun `can create and add videos to a collection`() {
            val video1 = saveVideo()
            val video2 = saveVideo()

            val collection = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "Collection vs Playlist",
                createdByBoclips = false,
                public = true
            )

            collectionRepository.update(
                collection.id,
                CollectionUpdateCommand.AddVideoToCollection(video1)
            )
            collectionRepository.update(
                collection.id,
                CollectionUpdateCommand.AddVideoToCollection(video2)
            )
            collectionRepository.update(
                collection.id,
                CollectionUpdateCommand.RemoveVideoFromCollection(video1)
            )

            val updatedCollection = collectionRepository.find(collection.id)!!

            assertThat(updatedCollection.owner).isEqualTo(
                UserId(
                    value = "user1"
                )
            )
            assertThat(updatedCollection.videos).hasSize(1)
            assertThat(updatedCollection.videos).contains(video2)
            assertThat(updatedCollection.title).isEqualTo("Collection vs Playlist")
            assertThat(updatedCollection.isPublic).isEqualTo(true)
            assertThat(updatedCollection.ageRange).isEqualTo(AgeRange.unbounded())
        }

        @Test
        fun `can create and then rename a collection`() {
            val collection = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "Starting Title",
                createdByBoclips = false,
                public = false
            )

            collectionRepository.update(collection.id, CollectionUpdateCommand.RenameCollection("New Title"))

            val updatedCollection = collectionRepository.find(collection.id)!!

            assertThat(updatedCollection.title).isEqualTo("New Title")
        }

        @Test
        fun `can create and mark a collection as public`() {
            val collection = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "Starting Title",
                createdByBoclips = false,
                public = false
            )
            assertThat(collection.isPublic).isEqualTo(false)

            collectionRepository.update(
                collection.id,
                CollectionUpdateCommand.ChangeVisibility(isPublic = true)
            )

            val updatedCollection = collectionRepository.find(collection.id)!!

            assertThat(updatedCollection.isPublic).isEqualTo(true)
        }

        @Test
        fun `can create a collection and replace subjects`() {
            val collection = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "Alex's Amazing Collection",
                createdByBoclips = false,
                public = false
            )

            val originalSubject = TestFactories.createSubject()

            collectionRepository.update(
                collection.id,
                CollectionUpdateCommand.ReplaceSubjects(setOf(originalSubject))
            )

            val newSubject = TestFactories.createSubject()

            collectionRepository.update(
                collection.id,
                CollectionUpdateCommand.ReplaceSubjects(setOf(newSubject))
            )

            val updatedCollection = collectionRepository.find(collection.id)

            assertThat(updatedCollection!!.subjects).containsExactly(newSubject)
        }

        @Test
        fun `can create and then change age range`() {
            val collection = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "Starting Title",
                createdByBoclips = false,
                public = false
            )

            collectionRepository.update(collection.id, CollectionUpdateCommand.ChangeAgeRange(3, 5))

            val updatedCollection = collectionRepository.find(collection.id)!!

            assertThat(updatedCollection.ageRange).isEqualTo(AgeRange.bounded(3, 5))
        }

        @Test
        fun `max age range can be null`() {
            val collection = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "Starting Title",
                createdByBoclips = false,
                public = false
            )

            collectionRepository.update(collection.id, CollectionUpdateCommand.ChangeAgeRange(3, null))

            val updatedCollection = collectionRepository.find(collection.id)!!

            assertThat(updatedCollection.ageRange).isEqualTo(AgeRange.bounded(3, null))
        }

        @Test
        fun `updatedAt timestamp on modifying changes`() {
            val video1 = saveVideo()

            val moment = Instant.now()
            val collectionV1 = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "My Videos",
                createdByBoclips = false,
                public = false
            )

            assertThat(collectionV1.updatedAt).isBetween(moment.minusSeconds(10), moment.plusSeconds(10))

            Thread.sleep(1)

            collectionRepository.update(
                collectionV1.id,
                CollectionUpdateCommand.AddVideoToCollection(video1)
            )

            val collectionV2 = collectionRepository.find(collectionV1.id)!!

            assertThat(collectionV2.updatedAt).isAfter(collectionV1.updatedAt)

            Thread.sleep(1)

            collectionRepository.update(
                collectionV2.id,
                CollectionUpdateCommand.RemoveVideoFromCollection(video1)
            )

            val collectionV3 = collectionRepository.find(collectionV1.id)!!

            assertThat(collectionV3.updatedAt).isAfter(collectionV2.updatedAt)
        }
    }

    @Nested
    inner class UpdateMany {
        @Test
        fun `removes a video reference from all collections`() {
            val videoId = VideoId(value = ObjectId().toHexString())

            val aGoodCollection = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "Great collection",
                createdByBoclips = false,
                public = false
            )

            collectionRepository.update(
                aGoodCollection.id,
                CollectionUpdateCommand.AddVideoToCollection(videoId)
            )

            val anotherGoodCollection = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "Good collection",
                createdByBoclips = true,
                public = false
            )

            collectionRepository.update(
                anotherGoodCollection.id,
                CollectionUpdateCommand.AddVideoToCollection(videoId)
            )

            collectionRepository.updateAll(CollectionsUpdateCommand.RemoveVideoFromAllCollections(videoId))

            assertThat(collectionRepository.find(aGoodCollection.id)!!.videos).isEmpty()
            assertThat(collectionRepository.find(anotherGoodCollection.id)!!.videos).isEmpty()
        }

        @Test
        fun `removes a subject from all collections`() {
            val aGoodCollection = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "Great collection",
                createdByBoclips = false,
                public = false
            )

            val subject = TestFactories.createSubject()
            val anotherSubject = TestFactories.createSubject()

            collectionRepository.update(
                aGoodCollection.id,
                CollectionUpdateCommand.ReplaceSubjects(setOf(subject, anotherSubject))
            )

            collectionRepository.updateAll(CollectionsUpdateCommand.RemoveSubjectFromAllCollections(subject.id))

            assertThat(collectionRepository.find(aGoodCollection.id)!!.subjects).containsExactly(anotherSubject)
        }
    }

    @Nested
    inner class DeleteCollections {
        @Test
        fun `can delete a collection`() {
            val collection = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "Starting Title",
                createdByBoclips = false,
                public = false
            )

            collectionRepository.delete(collection.id)

            val deletedCollection = collectionRepository.find(collection.id)

            assertThat(deletedCollection).isNull()
        }
    }

    @Nested
    inner class RetrieveCollections {
        @Test
        fun `can retrieve collection of user`() {
            val videoInCollection = saveVideo()
            val collection = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "",
                createdByBoclips = false,
                public = false
            )
            collectionRepository.update(
                collection.id,
                CollectionUpdateCommand.AddVideoToCollection(videoInCollection)
            )

            val userCollection = collectionRepository.getByOwner(
                UserId(
                    value = "user1"
                ), PageRequest(0, 10)
            )
            assertThat(userCollection.elements).hasSize(1)
            assertThat(userCollection.elements.map { it.id }).contains(collection.id)
        }

        @Test
        fun `can retrieve collections of viewer`() {
            val viewerId = "viewer1"
            val collection = collectionRepository.createWithViewers(
                owner = UserId(value = "user1"),
                title = "",
                viewerIds = listOf(viewerId)
            )

            val viewerCollections = collectionRepository.getByViewer(UserId(viewerId), PageRequest(0, 10))

            assertThat(viewerCollections.elements).hasSize(1)
            assertThat(viewerCollections.elements.map { it.id }).contains(collection.id)
        }

        @Test
        fun `can retrieve bookmarked collections`() {
            val publicBookmarkedCollection = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "Starting Title",
                createdByBoclips = false,
                public = false
            )

            val publicCollection2 = collectionRepository.create(
                owner = UserId(value = "user2"),
                title = "Starting Title",
                createdByBoclips = false,
                public = false
            )

            val privateCollection = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "Starting Title",
                createdByBoclips = false,
                public = false
            )

            collectionRepository.update(
                publicBookmarkedCollection.id,
                CollectionUpdateCommand.ChangeVisibility(true)
            )
            collectionRepository.bookmark(
                publicBookmarkedCollection.id,
                UserId("bookmarker")
            )
            collectionRepository.update(publicCollection2.id, CollectionUpdateCommand.ChangeVisibility(true))
            collectionRepository.bookmark(
                privateCollection.id,
                UserId("bookmarker")
            )

            val bookmarkedCollections = collectionRepository.getBookmarkedByUser(
                PageRequest(0, 10),
                UserId("bookmarker")
            )

            assertThat(bookmarkedCollections.elements).hasSize(1)
            assertThat(bookmarkedCollections.elements.map { it.id }).contains(publicBookmarkedCollection.id)
        }

        @Test
        fun `find will ignore invalid IDs`() {
            assertThat(collectionRepository.find(id = CollectionId(value = "1234"))).isNull()
        }

        @Test
        fun `findAll will ignore invalid IDs`() {
            assertThat(collectionRepository.findAll(ids = listOf(CollectionId(value = "1234")))).isEmpty()
        }
    }

    @Nested
    inner class MigrationTests {
        @Test
        fun `can retrieve legacy documents`() {
            mongoClient
                .getDatabase(DATABASE_NAME)
                .getCollection(MongoCollectionRepository.collectionName)
                .insertOne(
                    Document()
                        .append("_id", ObjectId("5c55697860fef77aa4af323a"))
                        .append("title", "My Videos")
                        .append("owner", "a4efeee2-0166-4371-ba72-0fa5a13c9aca")
                        .append("updatedAt", Date())
                        .append("videos", emptyList<VideoId>())
                )

            val collection = collectionRepository.find(CollectionId(value = "5c55697860fef77aa4af323a"))!!

            assertThat(collection.viewerIds).isEmpty()
            assertThat(collection.isPublic).isEqualTo(false)
        }

        @Test
        fun `can map viewerIds`() {
            val collectionId = "5c55697860fef77aa4af323a"
            val viewerId = "viewer-123@testing.com"
            mongoClient
                .getDatabase(DATABASE_NAME)
                .getCollection(MongoCollectionRepository.collectionName)
                .insertOne(
                    Document()
                        .append("_id", ObjectId(collectionId))
                        .append("title", "My Videos")
                        .append("owner", "a4efeee2-0166-4371-ba72-0fa5a13c9aca")
                        .append("viewerIds", listOf(viewerId))
                        .append("updatedAt", Date())
                        .append("videos", emptyList<VideoId>())
                )

            val collection = collectionRepository.find(CollectionId(value = collectionId))!!

            assertThat(collection.viewerIds).containsExactlyInAnyOrder(UserId(value = viewerId))
        }
    }

    @Nested
    inner class BookmarkingTests {
        @Test
        fun `can bookmark and unbookmark collections`() {
            setSecurityContext("user2")
            val collection = collectionRepository.create(
                owner = UserId(value = "user1"),
                title = "Collection vs Playlist",
                createdByBoclips = false,
                public = false
            )

            collectionRepository.bookmark(
                collection.id,
                UserId("user2")
            )
            collectionRepository.bookmark(
                collection.id,
                UserId("user3")
            )

            assertThat(collectionRepository.find(collection.id)!!.isBookmarked()).isEqualTo(true)

            collectionRepository.unbookmark(
                collection.id,
                UserId("user2")
            )

            assertThat(collectionRepository.find(collection.id)!!.isBookmarked()).isEqualTo(false)

            setSecurityContext("user3")
            assertThat(collectionRepository.find(collection.id)!!.isBookmarked()).isEqualTo(true)
        }
    }

    @Test
    fun `stream all public collections`() {
        val c1 = collectionRepository.create(
            owner = UserId(value = "user1"),
            title = "Starting Title",
            createdByBoclips = false,
            public = false
        )
        collectionRepository.create(
            owner = UserId(value = "user1"),
            title = "Starting Title",
            createdByBoclips = false,
            public = false
        )
        val c3 = collectionRepository.create(
            owner = UserId(value = "user1"),
            title = "Starting Title",
            createdByBoclips = false,
            public = false
        )
        collectionRepository.update(c1.id, CollectionUpdateCommand.ChangeVisibility(true))
        collectionRepository.update(c3.id, CollectionUpdateCommand.ChangeVisibility(true))

        var collections: List<Collection> = emptyList()
        collectionRepository.streamAllPublic { collections = it.toList() }

        assertThat(collections).hasSize(2)
    }
}

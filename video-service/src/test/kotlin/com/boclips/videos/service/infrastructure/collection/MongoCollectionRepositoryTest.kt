package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.UnknownAgeRange
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionFilter
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.domain.model.collection.CreateDefaultCollectionCommand
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.AttachmentFactory
import com.boclips.videos.service.testsupport.SubjectFactory
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZonedDateTime
import java.util.Date

class MongoCollectionRepositoryTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionRepository: MongoCollectionRepository

    @Nested
    inner class Find {
        @Test
        fun `findAll preserves order`() {
            val owner = UserId(value = "user1")
            val id1 =
                collectionRepository.create(
                    CreateCollectionCommand(
                        owner = owner,
                        title = "",
                        createdByBoclips = false,
                        discoverable = false
                    )
                ).id
            val id2 =
                collectionRepository.create(
                    CreateCollectionCommand(
                        owner = owner,
                        title = "",
                        createdByBoclips = false,
                        discoverable = false
                    )
                ).id
            val id3 =
                collectionRepository.create(
                    CreateCollectionCommand(
                        owner = owner,
                        title = "",
                        createdByBoclips = false,
                        discoverable = false
                    )
                ).id

            val collections = collectionRepository.findAll(listOf(id2, id3, id1))

            assertThat(collections.map { it.id }).containsExactly(id2, id3, id1)
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
    inner class CreateDefaultCollection {
        @Test
        fun `persists a default collection`() {
            val collection = collectionRepository.create(
                CreateDefaultCollectionCommand(
                    owner = UserId("some-owner")
                )
            )

            assertThat(collection.default).isTrue()
        }

        @Test
        fun `persists a default collection idempotently`() {
            val existingDefaultCollection = collectionRepository.create(
                CreateDefaultCollectionCommand(
                    owner = UserId("some-owner")
                )
            )

            val sameCollection = collectionRepository.create(
                CreateDefaultCollectionCommand(
                    owner = UserId("some-owner")
                )
            )

            assertThat(existingDefaultCollection).isEqualTo(sameCollection)
        }
    }

    @Nested
    inner class CreateAndUpdate {
        @Test
        fun `can create with subjects`() {
            val math = saveSubject("Math")
            val physics = saveSubject("Physics")

            val collection = collectionRepository.create(
                CreateCollectionCommand(
                    owner = UserId(value = "user1"),
                    title = "Collection vs Playlist",
                    createdByBoclips = false,
                    discoverable = true,
                    subjects = setOf(math.id, physics.id)
                )
            )

            assertThat(collection.subjects).containsExactlyInAnyOrder(math, physics)
        }

        @Test
        fun `can create and add videos to a collection`() {
            val video1 = saveVideo()
            val video2 = saveVideo()

            val collection = collectionRepository.create(
                CreateCollectionCommand(
                    owner = UserId(value = "user1"),
                    title = "Collection vs Playlist",
                    createdByBoclips = false,
                    discoverable = true
                )
            )

            collectionRepository.update(
                CollectionUpdateCommand.AddVideoToCollection(collection.id, video1, UserFactory.sample())
            )
            collectionRepository.update(
                CollectionUpdateCommand.AddVideoToCollection(collection.id, video2, UserFactory.sample())
            )
            collectionRepository.update(
                CollectionUpdateCommand.RemoveVideoFromCollection(collection.id, video1, UserFactory.sample())
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
            assertThat(updatedCollection.discoverable).isEqualTo(true)
            assertThat(updatedCollection.ageRange).isEqualTo(UnknownAgeRange)
        }

        @Test
        fun `can create and then rename a collection`() {
            val collection = collectionRepository.create(
                CreateCollectionCommand(
                    owner = UserId(value = "user1"),
                    title = "Starting Title",
                    createdByBoclips = false,
                    discoverable = false
                )
            )

            collectionRepository.update(
                CollectionUpdateCommand.RenameCollection(
                    collection.id,
                    "New Title",
                    UserFactory.sample()
                )
            )

            val updatedCollection = collectionRepository.find(collection.id)!!

            assertThat(updatedCollection.title).isEqualTo("New Title")
        }

        @Test
        fun `can create and mark a collection as public`() {
            val collection = collectionRepository.create(
                CreateCollectionCommand(
                    owner = UserId(value = "user1"),
                    title = "Starting Title",
                    createdByBoclips = false,
                    discoverable = false
                )
            )
            assertThat(collection.discoverable).isEqualTo(false)

            collectionRepository.update(
                CollectionUpdateCommand.ChangeDiscoverability(
                    collection.id,
                    discoverable = true,
                    user = UserFactory.sample()
                )
            )

            val updatedCollection = collectionRepository.find(collection.id)!!

            assertThat(updatedCollection.discoverable).isEqualTo(true)
        }

        @Test
        fun `can create a collection and replace subjects`() {
            val collection = collectionRepository.create(
                CreateCollectionCommand(
                    owner = UserId(value = "user1"),
                    title = "Alex's Amazing Collection",
                    createdByBoclips = false,
                    discoverable = false
                )
            )

            val originalSubject = TestFactories.createSubject()

            collectionRepository.update(
                CollectionUpdateCommand.ReplaceSubjects(collection.id, setOf(originalSubject), UserFactory.sample())
            )

            val newSubject = TestFactories.createSubject()

            collectionRepository.update(
                CollectionUpdateCommand.ReplaceSubjects(collection.id, setOf(newSubject), UserFactory.sample())
            )

            val updatedCollection = collectionRepository.find(collection.id)

            assertThat(updatedCollection!!.subjects).containsExactly(newSubject)
        }

        @Test
        fun `can create a collection and attach lesson plan`() {
            val collection = collectionRepository.create(
                CreateCollectionCommand(
                    owner = UserId(value = "user1"),
                    title = "Alex's Amazing Collection",
                    createdByBoclips = false,
                    discoverable = false
                )
            )

            val attachment = AttachmentFactory.sample()

            collectionRepository.update(
                CollectionUpdateCommand.AddAttachment(
                    collectionId = collection.id,
                    description = attachment.description,
                    linkToResource = attachment.linkToResource,
                    type = AttachmentType.LESSON_PLAN,
                    user = UserFactory.sample()
                )
            )

            val updatedCollection = collectionRepository.find(collection.id)

            assertThat(updatedCollection!!.attachments.first().description).isEqualTo(attachment.description)
            assertThat(updatedCollection.attachments.first().linkToResource).isEqualTo(attachment.linkToResource)
            assertThat(updatedCollection.attachments.first().type).isEqualTo(attachment.type)
        }

        @Test
        fun `can create and then change age range`() {
            val collection = collectionRepository.create(
                CreateCollectionCommand(
                    owner = UserId(value = "user1"),
                    title = "Starting Title",
                    createdByBoclips = false,
                    discoverable = false
                )
            )

            collectionRepository.update(
                CollectionUpdateCommand.ChangeAgeRange(collection.id, 3, 5, UserFactory.sample())
            )

            val updatedCollection = collectionRepository.find(collection.id)!!

            assertThat(updatedCollection.ageRange).isEqualTo(AgeRange.of(min = 3, max = 5, curatedManually = true))
        }

        @Test
        fun `can bookmark and unbookmark collections`() {
            val collection = collectionRepository.create(
                CreateCollectionCommand(
                    owner = UserId(value = "user1"),
                    title = "Collection vs Playlist",
                    createdByBoclips = false,
                    discoverable = false
                )
            )

            collectionRepository.update(
                CollectionUpdateCommand.Bookmark(
                    collection.id,
                    UserFactory.sample(id = "user2")
                )
            )
            collectionRepository.update(
                CollectionUpdateCommand.Bookmark(
                    collection.id,
                    UserFactory.sample(id = "user3")
                )
            )

            assertThat(collectionRepository.find(collection.id)!!.isBookmarkedBy(UserFactory.sample(id = "user2")))
                .isEqualTo(true)

            collectionRepository.update(
                CollectionUpdateCommand.Unbookmark(
                    collection.id,
                    UserFactory.sample(id = "user2")
                )
            )

            assertThat(collectionRepository.find(collection.id)!!.isBookmarkedBy(UserFactory.sample(id = "user2")))
                .isEqualTo(false)

            assertThat(collectionRepository.find(collection.id)!!.isBookmarkedBy(UserFactory.sample(id = "user3")))
                .isEqualTo(true)
        }

        @Test
        fun `bookmarking doesnt change updated time`() {
            val collection = collectionRepository.create(
                CreateCollectionCommand(
                    owner = UserId(value = "user1"),
                    title = "Collection vs Playlist",
                    createdByBoclips = false,
                    discoverable = false
                )
            )

            val updatedAt = collection.updatedAt

            Thread.sleep(1)

            collectionRepository.update(
                CollectionUpdateCommand.Bookmark(
                    collection.id,
                    UserFactory.sample(id = "user2")
                )
            )

            assertThat(collectionRepository.find(collection.id)?.updatedAt).isEqualTo(updatedAt)
        }

        @Test
        fun `max age range can be null`() {
            val collection = collectionRepository.create(
                CreateCollectionCommand(
                    owner = UserId(value = "user1"),
                    title = "Starting Title",
                    createdByBoclips = false,
                    discoverable = false
                )
            )

            collectionRepository.update(
                CollectionUpdateCommand.ChangeAgeRange(
                    collectionId = collection.id,
                    minAge = 3,
                    maxAge = null,
                    user = UserFactory.sample(id = "user2")
                )
            )

            val updatedCollection = collectionRepository.find(collection.id)!!

            assertThat(updatedCollection.ageRange).isEqualTo(
                AgeRange.of(
                    min = 3,
                    max = null,
                    curatedManually = true
                )
            )
        }

        @Test
        fun `can create a collection and then change its description`() {
            val collection = collectionRepository.create(
                CreateCollectionCommand(
                    owner = UserId(value = "user1"),
                    title = "Starting Title",
                    createdByBoclips = false,
                    discoverable = false
                )
            )

            collectionRepository.update(
                CollectionUpdateCommand.ChangeDescription(
                    collection.id,
                    "New collection description",
                    UserFactory.sample(id = "user2")
                )
            )

            val updatedCollection = collectionRepository.find(collection.id)!!

            assertThat(updatedCollection.description).isEqualTo("New collection description")
        }

        @Test
        fun `updatedAt timestamp on modifying changes`() {
            val video1 = saveVideo()

            val moment = ZonedDateTime.now()
            val collectionV1 = collectionRepository.create(
                CreateCollectionCommand(
                    owner = UserId(value = "user1"),
                    title = "My Videos",
                    createdByBoclips = false,
                    discoverable = false
                )
            )

            assertThat(collectionV1.updatedAt).isBetween(moment.minusSeconds(10), moment.plusSeconds(10))

            Thread.sleep(1)

            collectionRepository.update(
                CollectionUpdateCommand.AddVideoToCollection(
                    collectionV1.id,
                    video1,
                    UserFactory.sample(id = "user2")
                )
            )

            val collectionV2 = collectionRepository.find(collectionV1.id)!!

            assertThat(collectionV2.updatedAt).isAfter(collectionV1.updatedAt)

            Thread.sleep(1)

            collectionRepository.update(
                CollectionUpdateCommand.RemoveVideoFromCollection(
                    collectionV2.id,
                    video1,
                    UserFactory.sample(id = "user2")
                )
            )

            val collectionV3 = collectionRepository.find(collectionV1.id)!!

            assertThat(collectionV3.updatedAt).isAfter(collectionV2.updatedAt)
        }

        @Test
        fun `update returns updated collections`() {
            val collection1 = sampleCollection(title = "Old title 1")
            val collection2 = sampleCollection(title = "Old title 2")

            val result = collectionRepository.update(
                CollectionUpdateCommand.RenameCollection(
                    collection1.id,
                    "New title 1",
                    UserFactory.sample(id = "user2")
                ),
                CollectionUpdateCommand.RenameCollection(
                    collection2.id,
                    "New title 2",
                    UserFactory.sample(id = "user2")
                ),
                CollectionUpdateCommand.ChangeDiscoverability(
                    collection2.id,
                    true,
                    UserFactory.sample(id = "user2")
                )
            )

            assertThat(result).hasSize(2)
            assertThat(result.flatMap { it.commands }.size).isEqualTo(3)
        }

        @Test
        fun `update many`() {
            val collection = sampleCollection()
            val collection2 = sampleCollection()
            val collection3 = sampleCollection()

            val videoId = TestFactories.createVideoId()

            collectionRepository.update(
                CollectionUpdateCommand.ChangeDiscoverability(
                    collectionId = collection.id,
                    discoverable = true,
                    user = UserFactory.sample()
                ),
                CollectionUpdateCommand.RenameCollection(
                    collectionId = collection2.id,
                    title = "New Collection title",
                    user = UserFactory.sample()
                ),
                CollectionUpdateCommand.AddVideoToCollection(
                    collectionId = collection3.id,
                    videoId = videoId,
                    user = UserFactory.sample(id = "user2")
                )

                // TODO: Bulk adding videos to the same collection does not work :(
                // ,CollectionUpdateCommand.AddVideoToCollection(
                //     collectionId = collection3.id,
                //     videoId = secondVideoId,
                //     user = UserFactory.sample(id = "user2")
                // )
            )

            assertThat(collectionRepository.find(collection.id)!!.discoverable).isTrue()
            assertThat(collectionRepository.find(collection.id)!!.updatedAt).isAfterOrEqualTo(collection.updatedAt)
            assertThat(collectionRepository.find(collection2.id)!!.title).isEqualTo("New Collection title")
            assertThat(collectionRepository.find(collection2.id)!!.updatedAt).isAfterOrEqualTo(collection2.updatedAt)
            assertThat(collectionRepository.find(collection3.id)!!.videos[0]).isEqualTo(videoId)
            assertThat(collectionRepository.find(collection3.id)!!.updatedAt).isAfterOrEqualTo(collection3.updatedAt)
        }
    }

    @Nested
    inner class StreamingUpdate {
        @Test
        fun `stream update by filter`() {
            val collection = sampleCollection()

            val subject = SubjectFactory.sample(name = "French")
            val updatedSubject = SubjectFactory.sample(name = "Maths")

            collectionRepository.update(
                CollectionUpdateCommand.ReplaceSubjects(collection.id, setOf(subject), UserFactory.sample())
            )

            collectionRepository.streamUpdate(CollectionFilter.HasSubjectId(subject.id), { collectionToUpdate ->
                CollectionUpdateCommand.ReplaceSubjects(
                    collectionToUpdate.id,
                    setOf(updatedSubject),
                    UserFactory.sample()
                )
            })

            assertThat(collectionRepository.find(collection.id)!!.subjects).containsExactly(updatedSubject)
        }
    }

    @Nested
    inner class DeleteCollections {
        @Test
        fun `can delete a collection`() {
            val collection = collectionRepository.create(
                CreateCollectionCommand(
                    owner = UserId(value = "user1"),
                    title = "Starting Title",
                    createdByBoclips = false,
                    discoverable = false
                )
            )

            collectionRepository.delete(collection.id, UserFactory.sample())

            val deletedCollection = collectionRepository.find(collection.id)

            assertThat(deletedCollection).isNull()
        }
    }

    @Nested
    inner class CollectionWithUnits {
        @Test
        fun `can add, serialise and remove collections of a collection`() {
            val userId = UserId(value = "user1")

            val unit = collectionRepository.create(
                CreateCollectionCommand(
                    owner = userId,
                    title = "Unit Title",
                    createdByBoclips = false,
                    discoverable = false
                )
            )

            val collection = collectionRepository.create(
                CreateCollectionCommand(
                    owner = userId,
                    title = "Collection Title",
                    createdByBoclips = false,
                    discoverable = false
                )
            )

            collectionRepository.update(
                CollectionUpdateCommand.AddCollectionToCollection(
                    collectionId = collection.id,
                    subCollectionId = unit.id,
                    user = UserFactory.sample()
                )
            )

            val retrievedCollection = collectionRepository.find(collection.id)

            assertThat(retrievedCollection?.subCollections).isNotEmpty
            assertThat(retrievedCollection?.subCollections!![0].title).isEqualTo("Unit Title")

            collectionRepository.update(
                CollectionUpdateCommand.RemoveCollectionFromCollection(
                    collectionId = collection.id,
                    subCollectionId = unit.id,
                    user = UserFactory.sample()
                )
            )

            assertThat(collectionRepository.find(collection.id)!!.subCollections).isEmpty()
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

            assertThat(collection.discoverable).isEqualTo(false)
        }
    }

    @Test
    fun `stream all collections`() {
        val c1 = collectionRepository.create(
            CreateCollectionCommand(
                owner = UserId(value = "user1"),
                title = "Starting Title",
                createdByBoclips = false,
                discoverable = false
            )
        )
        collectionRepository.create(
            CreateCollectionCommand(
                owner = UserId(value = "user1"),
                title = "Starting Title",
                createdByBoclips = false,
                discoverable = false
            )
        )
        val c3 = collectionRepository.create(
            CreateCollectionCommand(
                owner = UserId(value = "user1"),
                title = "Starting Title",
                createdByBoclips = false,
                discoverable = false
            )
        )
        collectionRepository.update(
            CollectionUpdateCommand.ChangeDiscoverability(
                c1.id,
                true,
                UserFactory.sample()
            )
        )
        collectionRepository.update(
            CollectionUpdateCommand.ChangeDiscoverability(
                c3.id,
                true,
                UserFactory.sample()
            )
        )

        var collections: List<Collection> = emptyList()
        collectionRepository.streamAll { collections = it.toList() }

        assertThat(collections).hasSize(3)
    }

    private fun sampleCollection(
        owner: UserId = UserId(
            value = "user1"
        ),
        title: String = "Collection title",
        createdByBoclips: Boolean = false,
        public: Boolean = false
    ): Collection {
        return collectionRepository.create(
            CreateCollectionCommand(
                owner = owner,
                title = title,
                createdByBoclips = createdByBoclips,
                discoverable = public
            )
        )
    }
}

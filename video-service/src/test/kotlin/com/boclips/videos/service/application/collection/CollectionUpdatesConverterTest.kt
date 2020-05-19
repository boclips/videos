package com.boclips.videos.service.application.collection

import com.boclips.videos.api.request.agerange.AgeRangeRequest
import com.boclips.videos.api.request.attachments.AttachmentRequest
import com.boclips.videos.api.request.collection.UpdateCollectionRequest
import com.boclips.videos.service.application.collection.exceptions.InvalidAttachmentTypeException
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CollectionUpdatesConverterTest {
    private lateinit var collectionUpdatesConverter: CollectionUpdatesConverter
    private lateinit var subjectRepositoryMock: SubjectRepository

    @BeforeEach
    fun setUp() {
        subjectRepositoryMock = mock()
        collectionUpdatesConverter = CollectionUpdatesConverter(subjectRepositoryMock)
    }

    @Test
    fun `can handle null request`() {
        val commands = collectionUpdatesConverter.convert(CollectionId("testId"), null, UserFactory.sample())

        assertThat(commands).hasSize(0)
    }

    @Test
    fun `turn title change to command`() {
        val commands =
            collectionUpdatesConverter.convert(
                CollectionId("testId"),
                UpdateCollectionRequest(title = "some title"),
                UserFactory.sample()
            )

        assertThat(commands.first()).isInstanceOf(CollectionUpdateCommand.RenameCollection::class.java)
        assertThat(commands).hasSize(1)
    }

    @Test
    fun `change public visibility of collection to command`() {
        val commands =
            collectionUpdatesConverter.convert(
                CollectionId("testId"),
                UpdateCollectionRequest(discoverable = true),
                UserFactory.sample()
            )

        val command = commands.first() as CollectionUpdateCommand.ChangeDiscoverability
        assertThat(command.discoverable).isEqualTo(true)
        assertThat(commands).hasSize(1)
    }

    @Test
    fun `change private visibility of collection to command`() {
        val commands =
            collectionUpdatesConverter.convert(
                CollectionId("testId"),
                UpdateCollectionRequest(discoverable = false),
                UserFactory.sample()
            )

        val command = commands.first() as CollectionUpdateCommand.ChangeDiscoverability
        assertThat(command.discoverable).isEqualTo(false)
        assertThat(commands).hasSize(1)
    }

    @Test
    fun `converts multiple changes to commands`() {
        val commands =
            collectionUpdatesConverter.convert(
                CollectionId("testId"),
                UpdateCollectionRequest(title = "some title", discoverable = true),
                UserFactory.sample()
            )

        assertThat(commands).hasSize(2)
    }

    @Test
    fun `change age range of collection to command`() {
        val commands =
            collectionUpdatesConverter.convert(
                CollectionId("testId"),
                UpdateCollectionRequest(
                    ageRange = AgeRangeRequest(
                        min = 3,
                        max = 5
                    )
                ),
                UserFactory.sample()
            )

        val command = commands.first() as CollectionUpdateCommand.ChangeAgeRange
        assertThat(command.minAge).isEqualTo(3)
        assertThat(command.maxAge).isEqualTo(5)
    }

    @Test
    fun `change age range of collection command with unbounded upper bound`() {
        val commands = collectionUpdatesConverter.convert(
            CollectionId("testId"),
            UpdateCollectionRequest(
                ageRange = AgeRangeRequest(
                    min = 18,
                    max = null
                )
            ),
            UserFactory.sample()
        )

        val command = commands.first() as CollectionUpdateCommand.ChangeAgeRange
        assertThat(command.minAge).isEqualTo(18)
        assertThat(command.maxAge).isNull()
    }

    @Test
    fun `does not change age range where min and max are null`() {
        val commands = collectionUpdatesConverter.convert(
            CollectionId("testId"),
            UpdateCollectionRequest(
                ageRange = AgeRangeRequest(
                    min = null,
                    max = null
                )
            ),
            UserFactory.sample()
        )

        assertThat(commands).isEmpty()
    }

    @Test
    fun `turn subjects update to command`() {
        val subject = TestFactories.createSubject()
        whenever(subjectRepositoryMock.findById(any())).thenReturn(subject)
        val commands = collectionUpdatesConverter.convert(
            CollectionId("testId"),
            UpdateCollectionRequest(subjects = setOf("SubjectOneId")),
            UserFactory.sample()
        )

        assertThat(commands).hasSize(1)
        assertThat(commands.first()).isInstanceOf(CollectionUpdateCommand.ReplaceSubjects::class.java)
        val command = commands.first() as CollectionUpdateCommand.ReplaceSubjects

        assertThat(command.subjects).isEqualTo(setOf(subject))
    }

    @Test
    fun `Turn description into command`() {
        val commands = collectionUpdatesConverter.convert(
            CollectionId("testId"),
            UpdateCollectionRequest(
                description = "New description"
            ),
            UserFactory.sample()
        )

        val command = commands.first() as CollectionUpdateCommand.ChangeDescription
        assertThat(command.description).isEqualTo("New description")
    }

    @Test
    fun `turns list of videos into a command`() {
        val collectionId = CollectionId("testId")

        val firstId = ObjectId().toHexString()
        val secondId = ObjectId().toHexString()
        val thirdId = ObjectId().toHexString()

        val commands = collectionUpdatesConverter.convert(
            collectionId,
            UpdateCollectionRequest(
                videos = listOf(firstId, secondId, thirdId)
            ),
            UserFactory.sample()
        )

        val command = commands.first() as CollectionUpdateCommand.BulkUpdateCollectionVideos
        assertThat(command.collectionId).isEqualTo(collectionId)
        assertThat(command.videoIds).containsExactlyInAnyOrder(
            VideoId(firstId),
            VideoId(secondId),
            VideoId(thirdId)
        )
    }

    @Test
    fun `Turn attachments update into command`() {
        val commands = collectionUpdatesConverter.convert(
            CollectionId("testId"),
            UpdateCollectionRequest(
                attachment = AttachmentRequest(
                    linkToResource = "www.lesson-plan.com",
                    description = "new description",
                    type = "LESSON_PLAN"
                )
            ),
            UserFactory.sample()
        )

        val command = commands.first() as CollectionUpdateCommand.AddAttachment
        assertThat(command.collectionId.value).isEqualTo("testId")
        assertThat(command.linkToResource).isEqualTo("www.lesson-plan.com")
        assertThat(command.description).isEqualTo("new description")
        assertThat(command.type).isEqualTo(AttachmentType.LESSON_PLAN)
    }

    @Test
    fun `invalid attachment type throws an exception`() {
        assertThrows<InvalidAttachmentTypeException> {
            collectionUpdatesConverter.convert(
                CollectionId("testId"),
                UpdateCollectionRequest(
                    attachment = AttachmentRequest(
                        linkToResource = "www.lesson-plan.com",
                        description = "new description",
                        type = "INVALID"
                    )
                ),
                UserFactory.sample()
            )
        }
    }
}

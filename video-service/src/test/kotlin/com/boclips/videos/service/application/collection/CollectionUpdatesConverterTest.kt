package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.subject.SubjectRepository
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectionUpdatesConverterTest {
    private lateinit var collectionUpdatesConverter: CollectionUpdatesConverter
    private lateinit var subjectRepositoryMock: SubjectRepository

    @BeforeEach
    fun setUp() {
        subjectRepositoryMock = mock<SubjectRepository>()
        collectionUpdatesConverter = CollectionUpdatesConverter(subjectRepositoryMock)
    }

    @Test
    fun `can handle null request`() {
        val commands = collectionUpdatesConverter.convert(null)

        assertThat(commands).hasSize(0)
    }

    @Test
    fun `turn title change to command`() {
        val commands = collectionUpdatesConverter.convert(UpdateCollectionRequest(title = "some title"))

        assertThat(commands.first()).isInstanceOf(CollectionUpdateCommand.RenameCollection::class.java)
        assertThat(commands).hasSize(1)
    }

    @Test
    fun `change public visibility of collection to command`() {
        val commands = collectionUpdatesConverter.convert(UpdateCollectionRequest(isPublic = true))

        val command = commands.first() as CollectionUpdateCommand.ChangeVisibility
        assertThat(command.isPublic).isEqualTo(true)
        assertThat(commands).hasSize(1)
    }

    @Test
    fun `change private visibility of collection to command`() {
        val commands = collectionUpdatesConverter.convert(UpdateCollectionRequest(isPublic = false))

        val command = commands.first() as CollectionUpdateCommand.ChangeVisibility
        assertThat(command.isPublic).isEqualTo(false)
        assertThat(commands).hasSize(1)
    }

    @Test
    fun `converts multiple changes to commands`() {
        val commands =
            collectionUpdatesConverter.convert(UpdateCollectionRequest(title = "some title", isPublic = true))

        assertThat(commands).hasSize(2)
    }

    @Test
    fun `change age range of collection to command`() {
        val commands =
            collectionUpdatesConverter.convert(UpdateCollectionRequest(ageRange = AgeRangeRequest(min = 3, max = 5)))

        val command = commands.first() as CollectionUpdateCommand.ChangeAgeRange
        assertThat(command.minAge).isEqualTo(3)
        assertThat(command.maxAge).isEqualTo(5)
    }

    @Test
    fun `change age range of collection command with unbounded upper bound`() {
        val commands = collectionUpdatesConverter.convert(
            UpdateCollectionRequest(
                ageRange = AgeRangeRequest(
                    min = 18,
                    max = null
                )
            )
        )

        val command = commands.first() as CollectionUpdateCommand.ChangeAgeRange
        assertThat(command.minAge).isEqualTo(18)
        assertThat(command.maxAge).isNull()
    }

    @Test
    fun `does not change age range where min and max are null`() {
        val commands = collectionUpdatesConverter.convert(
            UpdateCollectionRequest(
                ageRange = AgeRangeRequest(
                    min = null,
                    max = null
                )
            )
        )

        assertThat(commands).isEmpty()
    }

    @Test
    fun `turn subjects update to command`() {
        val subject = TestFactories.createSubject()
        whenever(subjectRepositoryMock.findById(any())).thenReturn(subject)
        val commands = collectionUpdatesConverter.convert(
            UpdateCollectionRequest(subjects = setOf("SubjectOneId"))
        )

        assertThat(commands).hasSize(1)
        assertThat(commands.first()).isInstanceOf(CollectionUpdateCommand.ReplaceSubjects::class.java)
        val command = commands.first() as CollectionUpdateCommand.ReplaceSubjects

        assertThat(command.subjects).isEqualTo(setOf(subject))
    }

    @Test
    fun `Turn description into command`() {
        val commands = collectionUpdatesConverter.convert(
            UpdateCollectionRequest(
                description = "New description"
            )
        )

        val command = commands.first() as CollectionUpdateCommand.ChangeDescription
        assertThat(command.description).isEqualTo("New description")
    }
}

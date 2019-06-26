package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.subjects.SubjectId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CollectionUpdatesConverterTest {
    @Test
    fun `can handle null request`() {
        val commands = CollectionUpdatesConverter.convert(null)

        assertThat(commands).hasSize(0)
    }

    @Test
    fun `turn title change to command`() {
        val commands = CollectionUpdatesConverter.convert(UpdateCollectionRequest(title = "some title"))

        assertThat(commands.first()).isInstanceOf(CollectionUpdateCommand.RenameCollectionCommand::class.java)
        assertThat(commands).hasSize(1)
    }

    @Test
    fun `change public visibility of collection to command`() {
        val commands = CollectionUpdatesConverter.convert(UpdateCollectionRequest(isPublic = true))

        val command = commands.first() as CollectionUpdateCommand.ChangeVisibilityCommand
        assertThat(command.isPublic).isEqualTo(true)
        assertThat(commands).hasSize(1)
    }

    @Test
    fun `change private visibility of collection to command`() {
        val commands = CollectionUpdatesConverter.convert(UpdateCollectionRequest(isPublic = false))

        val command = commands.first() as CollectionUpdateCommand.ChangeVisibilityCommand
        assertThat(command.isPublic).isEqualTo(false)
        assertThat(commands).hasSize(1)
    }

    @Test
    fun `converts multiple changes to commands`() {
        val commands =
            CollectionUpdatesConverter.convert(UpdateCollectionRequest(title = "some title", isPublic = true))

        assertThat(commands).hasSize(2)
    }

    @Test
    fun `change age range of collection to command`() {
        val commands =
            CollectionUpdatesConverter.convert(UpdateCollectionRequest(ageRange = AgeRangeRequest(min = 3, max = 5)))

        val command = commands.first() as CollectionUpdateCommand.ChangeAgeRangeCommand
        assertThat(command.minAge).isEqualTo(3)
        assertThat(command.maxAge).isEqualTo(5)
    }

    @Test
    fun `change age range of collection command with unbounded upper bound`() {
        val commands = CollectionUpdatesConverter.convert(
            UpdateCollectionRequest(
                ageRange = AgeRangeRequest(
                    min = 18,
                    max = null
                )
            )
        )

        val command = commands.first() as CollectionUpdateCommand.ChangeAgeRangeCommand
        assertThat(command.minAge).isEqualTo(18)
        assertThat(command.maxAge).isNull()
    }

    @Test
    fun `does not change age range where min and max are null`() {
        val commands = CollectionUpdatesConverter.convert(
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
        val commands = CollectionUpdatesConverter.convert(
            UpdateCollectionRequest(
                subjects = setOf(
                    "SubjectOneId",
                    "SubjectTwoId"
                )
            )
        )

        assertThat(commands).hasSize(1)
        assertThat(commands.first()).isInstanceOf(CollectionUpdateCommand.ReplaceSubjectsCommand::class.java)
        val command = commands.first() as CollectionUpdateCommand.ReplaceSubjectsCommand

        assertThat(command.subjects).isEqualTo(
            setOf(
                SubjectId("SubjectOneId"),
                SubjectId("SubjectTwoId")
            )
        )
    }
}

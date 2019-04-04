package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CollectionUpdatesConverterTest {
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
}
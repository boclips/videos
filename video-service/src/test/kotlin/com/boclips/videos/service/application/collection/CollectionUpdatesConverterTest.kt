package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.service.collection.RenameCollectionCommand
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
    fun `convert title change to command`() {
        val commands = CollectionUpdatesConverter.convert(UpdateCollectionRequest(title = "some title"))

        assertThat(commands.first()).isInstanceOf(RenameCollectionCommand::class.java)
    }
}
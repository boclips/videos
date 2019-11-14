package com.boclips.videos.service.domain.service.collection

import com.boclips.eventbus.events.collection.CollectionCreated
import com.boclips.eventbus.events.collection.CollectionDeleted
import com.boclips.eventbus.events.collection.CollectionUpdated
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CollectionRepositoryEventsDecoratorTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var repository: CollectionRepositoryEventsDecorator

    @Test
    fun `publishes collection created event when collection is created`() {
        repository.create(
            CreateCollectionCommand(
            owner = UserId(TestFactories.aValidId()),
                title = "My new collection",
                createdByBoclips = false,
                public = false
        ))

        assertThat(fakeEventBus.countEventsOfType(CollectionCreated::class.java)).isEqualTo(1)
        assertThat(fakeEventBus.getEventOfType(CollectionCreated::class.java).collection.title).isEqualTo("My new collection")
    }

    @Test
    fun `publishes collection updated event when collection is updated`() {
        val video = saveVideo()
        val collection = saveCollection()

        repository.update(CollectionUpdateCommand.AddVideoToCollection(collection, video))

        assertThat(fakeEventBus.countEventsOfType(CollectionUpdated::class.java)).isEqualTo(1)
        assertThat(fakeEventBus.getEventOfType(CollectionUpdated::class.java).collection.videosIds).hasSize(1)
        assertThat(fakeEventBus.getEventOfType(CollectionUpdated::class.java).collection.videosIds.first().value).isEqualTo(video.value)
    }

    @Test
    fun `publishes collection updated events when collections are updated in a streaming fashion`() {
        val video = saveVideo()
        saveCollection(videos = listOf(video.value))
        saveCollection(videos = listOf(video.value))
        saveCollection(videos = listOf())

        repository.streamUpdate(CollectionFilter.HasVideoId(video), { collectionToUpdate ->
            CollectionUpdateCommand.RenameCollection(collectionToUpdate.id, "The new title")
        })

        assertThat(fakeEventBus.countEventsOfType(CollectionUpdated::class.java)).isEqualTo(2)
        assertThat(fakeEventBus.getEventsOfType(CollectionUpdated::class.java).first().collection.title).isEqualTo("The new title")
    }

    @Test
    fun `publishes collection deleted events when collections are deleted`() {
        val collection = saveCollection()

        repository.delete(collection)

        assertThat(fakeEventBus.countEventsOfType(CollectionDeleted::class.java)).isEqualTo(1)
        assertThat(fakeEventBus.getEventOfType(CollectionDeleted::class.java).collectionId).isEqualTo(collection.value)
    }
}

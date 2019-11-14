package com.boclips.videos.service.domain.service.collection

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.collection.CollectionCreated
import com.boclips.eventbus.events.collection.CollectionUpdated
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.EventConverter

class EventPublishingCollectionRepository(
    private val collectionRepository: CollectionRepository,
    private val eventBus: EventBus
)
    : CollectionRepository by collectionRepository {

    override fun create(command: CreateCollectionCommand): Collection {
        return collectionRepository.create(command)
            .also(this::publishCollectionCreated)
    }

    override fun update(command: CollectionUpdateCommand): Collection {
        return collectionRepository.update(command)
            .also(this::publishCollectionUpdated)
    }

    override fun bulkUpdate(commands: List<CollectionUpdateCommand>): List<Collection> {
        return collectionRepository.bulkUpdate(commands)
            .also(this::publishCollectionsUpdated)
    }

    override fun streamUpdate(
        filter: CollectionFilter,
        updateCommandFactory: (List<Collection>) -> List<CollectionUpdateCommand>,
        updatedCollectionsConsumer: (List<Collection>) -> Unit
    ) {
        return collectionRepository.streamUpdate(filter, updateCommandFactory) { updatedCollections ->
            publishCollectionsUpdated(updatedCollections)
            updatedCollectionsConsumer(updatedCollections)
        }
    }

    override fun updateAll(
        updateCommand: CollectionsUpdateCommand,
        updatedCollectionsConsumer: (List<Collection>) -> Unit
    ) {
        collectionRepository.updateAll(updateCommand) { updatedCollections ->
            publishCollectionsUpdated(updatedCollections)
            updatedCollectionsConsumer(updatedCollections)
        }
    }

    private fun publishCollectionsUpdated(collections: List<Collection>) {
        collections.forEach(this::publishCollectionUpdated)
    }

    private fun publishCollectionUpdated(collection: Collection) {
        val event = CollectionUpdated(
            EventConverter().toCollectionPayload(collection)
        )
        eventBus.publish(event)
    }

    private fun publishCollectionCreated(collection: Collection) {
        val event = CollectionCreated(
            EventConverter().toCollectionPayload(collection)
        )
        eventBus.publish(event)
    }

}

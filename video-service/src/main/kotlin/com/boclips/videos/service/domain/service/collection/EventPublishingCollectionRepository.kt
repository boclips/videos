package com.boclips.videos.service.domain.service.collection

import com.boclips.eventbus.events.collection.CollectionCreated
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionUpdateResult
import com.boclips.videos.service.domain.service.EventConverter
import com.boclips.videos.service.domain.service.events.EventService

class EventPublishingCollectionRepository(
    private val collectionRepository: CollectionRepository,
    private val eventService: EventService
)
    : CollectionRepository by collectionRepository {

    override fun create(command: CreateCollectionCommand): Collection {
        return collectionRepository.create(command)
            .also { collection -> eventService.saveCollectionCreatedEvent(collection) }
    }

    override fun update(vararg commands: CollectionUpdateCommand): List<CollectionUpdateResult> {
        return collectionRepository.update(*commands)
            .also { results -> this.publishCollectionsUpdated(results) }
    }

    override fun streamUpdate(
        filter: CollectionFilter,
        updateCommandFactory: (Collection) -> CollectionUpdateCommand,
        updateResultConsumer: (CollectionUpdateResult) -> Unit
    ) {
        return collectionRepository.streamUpdate(filter, updateCommandFactory) { update ->
            publishCollectionUpdated(update)
            updateResultConsumer(update)
        }
    }

    override fun delete(id: CollectionId) {
        collectionRepository.delete(id)
        eventService.saveCollectionDeletedEvent(id)
    }

    private fun publishCollectionsUpdated(updates: List<CollectionUpdateResult>) {
        updates.forEach(this::publishCollectionUpdated)
    }

    private fun publishCollectionUpdated(update: CollectionUpdateResult) {
        eventService.saveUpdateCollectionEvent(update)
    }
}

package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionFilter
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.events.EventService

class CollectionRepositoryEventsDecorator(
    private val collectionRepository: CollectionRepository,
    private val eventService: EventService
) : CollectionRepository by collectionRepository {

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

    override fun delete(id: CollectionId, user: User) {
        collectionRepository.delete(id, user)
        eventService.saveCollectionDeletedEvent(id, user)
    }

    private fun publishCollectionsUpdated(updates: List<CollectionUpdateResult>) {
        updates.forEach(this::publishCollectionUpdated)
    }

    private fun publishCollectionUpdated(update: CollectionUpdateResult) {
        eventService.saveUpdateCollectionEvent(update)
    }
}

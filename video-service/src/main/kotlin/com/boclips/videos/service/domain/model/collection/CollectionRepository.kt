package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.service.collection.CollectionFilter
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.collection.CreateCollectionCommand

data class CollectionUpdateResult(val collection: Collection, val commands: List<CollectionUpdateCommand>)

interface CollectionRepository {
    fun find(id: CollectionId): Collection?
    fun findAll(ids: List<CollectionId>): List<Collection>
    fun streamAll(consumer: (Sequence<Collection>) -> Unit)
    fun create(command: CreateCollectionCommand): Collection
    fun update(vararg commands: CollectionUpdateCommand): List<CollectionUpdateResult>
    fun streamUpdate(
        filter: CollectionFilter,
        updateCommandFactory: (Collection) -> CollectionUpdateCommand,
        updateResultConsumer: (CollectionUpdateResult) -> Unit = {}
    )

    fun delete(id: CollectionId, user: User)
}

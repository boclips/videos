package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionFilter
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.domain.model.collection.CreateDefaultCollectionCommand
import com.boclips.videos.service.domain.model.user.User

interface CollectionRepository {
    fun find(id: CollectionId): Collection?
    fun findAll(ids: List<CollectionId>): List<Collection>
    fun streamAll(consumer: (Sequence<Collection>) -> Unit)
    fun create(command: CreateCollectionCommand): Collection
    fun create(command: CreateDefaultCollectionCommand): Collection
    fun update(vararg commands: CollectionUpdateCommand): List<CollectionUpdateResult>
    fun streamUpdate(
        filter: CollectionFilter,
        updateCommandFactory: (Collection) -> CollectionUpdateCommand,
        updateResultConsumer: (CollectionUpdateResult) -> Unit = {}
    )

    fun delete(id: CollectionId, user: User)
}

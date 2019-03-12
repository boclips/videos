package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId

interface CollectionService {
    fun create(owner: UserId, title: String): Collection
    fun getById(id: CollectionId): Collection?
    fun getByOwner(owner: UserId): List<Collection>
    fun update(id: CollectionId, updateCommand: CollectionUpdateCommand)
    fun update(id: CollectionId, updateCommands: List<CollectionUpdateCommand>)
    fun delete(id: CollectionId)
}
package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId

interface CollectionService {
    fun create(owner: UserId): Collection

    fun getById(id: CollectionId): Collection?

    fun getByOwner(owner: UserId): List<Collection>

    fun update(id: CollectionId, updateCommand: CollectionUpdateCommand)
}
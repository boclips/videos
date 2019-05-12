package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.UserId

interface CollectionRepository {
    fun create(owner: UserId, title: String, createdByBoclips: Boolean): Collection
    fun getById(id: CollectionId): Collection?
    fun getByOwner(owner: UserId, pageRequest: PageRequest): Page<Collection>
    fun update(id: CollectionId, updateCommand: CollectionUpdateCommand)
    fun update(id: CollectionId, updateCommands: List<CollectionUpdateCommand>)
    fun delete(id: CollectionId)
    fun getPublic(pageRequest: PageRequest): Page<Collection>
    fun getBookmarked(pageRequest: PageRequest, bookmarkedBy: UserId): Page<Collection>
    fun bookmark(id: CollectionId, user: UserId)
    fun unbookmark(id: CollectionId, user: UserId)
}
package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand

interface CollectionRepository {
    fun create(owner: UserId, title: String, createdByBoclips: Boolean): Collection
    fun find(id: CollectionId): Collection?
    fun findAll(id: List<CollectionId>): List<Collection>
    fun streamAllPublic(consumer: (Sequence<Collection>) -> Unit)
    fun getByOwner(owner: UserId, pageRequest: PageRequest): Page<Collection>
    fun update(id: CollectionId, vararg updateCommands: CollectionUpdateCommand)
    fun delete(id: CollectionId)
    fun getPublic(pageRequest: PageRequest, subjects: List<SubjectId> = emptyList()): Page<Collection>
    fun getBookmarked(pageRequest: PageRequest, bookmarkedBy: UserId): Page<Collection>
    fun bookmark(id: CollectionId, user: UserId)
    fun unbookmark(id: CollectionId, user: UserId)
}
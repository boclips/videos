package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.subjects.SubjectId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.collection.CollectionsUpdateCommand

interface CollectionRepository {
    fun find(id: CollectionId): Collection?
    fun findAll(ids: List<CollectionId>): List<Collection>
    fun findAllBySubject(subjectId: SubjectId): List<Collection>
    fun streamAllPublic(consumer: (Sequence<Collection>) -> Unit)
    fun getByOwner(owner: UserId, pageRequest: PageRequest): Page<Collection>
    fun getBookmarkedByUser(pageRequest: PageRequest, bookmarkedBy: UserId): Page<Collection>
    fun create(owner: UserId, title: String, createdByBoclips: Boolean, public: Boolean): Collection
    fun update(collectionId: CollectionId, vararg updateCommands: CollectionUpdateCommand)
    fun updateAll(updateCommand: CollectionsUpdateCommand)
    fun delete(id: CollectionId)
    fun bookmark(id: CollectionId, user: UserId)
    fun unbookmark(id: CollectionId, user: UserId)
}

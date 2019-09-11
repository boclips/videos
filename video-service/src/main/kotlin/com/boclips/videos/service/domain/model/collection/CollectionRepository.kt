package com.boclips.videos.service.domain.model.collection

import com.boclips.users.client.model.contract.Contract
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.service.collection.CollectionFilter
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.collection.CollectionsUpdateCommand

interface CollectionRepository {
    fun find(id: CollectionId): Collection?
    fun findAll(ids: List<CollectionId>): List<Collection>
    fun findAllBySubject(subjectId: SubjectId): List<Collection>
    fun streamAllPublic(consumer: (Sequence<Collection>) -> Unit)
    fun getByOwner(owner: UserId, pageRequest: PageRequest): Page<Collection>
    fun getByViewer(viewer: UserId, pageRequest: PageRequest): Page<Collection>
    fun getByContracts(contracts: List<Contract>, pageRequest: PageRequest): Page<Collection>
    fun getBookmarkedByUser(pageRequest: PageRequest, bookmarkedBy: UserId): Page<Collection>
    fun create(owner: UserId, title: String, createdByBoclips: Boolean, public: Boolean): Collection
    fun createWithViewers(owner: UserId, title: String, viewerIds: List<String>): Collection
    fun update(command: CollectionUpdateCommand)
    fun bulkUpdate(commands: List<CollectionUpdateCommand>): List<Collection>
    fun updateAll(updateCommand: CollectionsUpdateCommand)
    fun streamUpdate(filter: CollectionFilter, consumer: (List<Collection>) -> List<CollectionUpdateCommand>)
    fun delete(id: CollectionId)
    fun bookmark(id: CollectionId, user: UserId)
    fun unbookmark(id: CollectionId, user: UserId)
}

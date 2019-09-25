package com.boclips.videos.service.domain.model.collection

import com.boclips.users.client.model.contract.Contract
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.service.collection.CollectionFilter
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.collection.CollectionsUpdateCommand
import com.boclips.videos.service.domain.service.collection.CreateCollectionCommand

interface CollectionRepository {
    fun find(id: CollectionId): Collection?
    fun findAll(ids: List<CollectionId>): List<Collection>
    fun findAllBySubject(subjectId: SubjectId): List<Collection>
    fun streamAll(consumer: (Sequence<Collection>) -> Unit)
    fun getByContracts(contracts: List<Contract>, pageRequest: PageRequest): Page<Collection>
    fun create(command: CreateCollectionCommand): Collection
    fun update(command: CollectionUpdateCommand)
    fun bulkUpdate(commands: List<CollectionUpdateCommand>): List<Collection>
    fun updateAll(updateCommand: CollectionsUpdateCommand)
    fun streamUpdate(filter: CollectionFilter, consumer: (List<Collection>) -> List<CollectionUpdateCommand>)
    fun delete(id: CollectionId)
    fun bookmark(id: CollectionId, user: UserId): Collection
    fun unbookmark(id: CollectionId, user: UserId): Collection
}

package com.boclips.videos.service.domain.model.collection

import com.boclips.security.utils.User
import com.boclips.users.client.model.contract.Contract
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.service.collection.CollectionFilter
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.collection.CreateCollectionCommand

data class CollectionUpdateResult(val collection: Collection, val commands: List<CollectionUpdateCommand>)

interface CollectionRepository {
    fun find(id: CollectionId): Collection?
    fun findAll(ids: List<CollectionId>): List<Collection>
    fun findAllBySubject(subjectId: SubjectId): List<Collection>
    fun streamAll(consumer: (Sequence<Collection>) -> Unit)
    fun getByContracts(contracts: List<Contract>, pageRequest: PageRequest): Page<Collection>
    fun create(command: CreateCollectionCommand): Collection
    fun update(vararg commands: CollectionUpdateCommand): List<CollectionUpdateResult>
    fun streamUpdate(
        filter: CollectionFilter,
        updateCommandFactory: (Collection) -> CollectionUpdateCommand,
        updateResultConsumer: (CollectionUpdateResult) -> Unit = {}
    )

    fun delete(id: CollectionId, user: User)
}

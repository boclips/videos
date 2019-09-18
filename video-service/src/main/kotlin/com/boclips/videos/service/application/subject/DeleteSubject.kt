package com.boclips.videos.service.application.subject

import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionsUpdateCommand
import com.boclips.videos.service.domain.service.subject.SubjectRepository

class DeleteSubject(
    private val subjectRepository: SubjectRepository,
    private val collectionRepository: CollectionRepository,
    private val collectionSearchService: CollectionSearchService
) {
    operator fun invoke(subjectId: SubjectId) {
        subjectRepository.delete(subjectId)

        val idsOfCollectionsContainingSubject = collectionRepository
            .findAllBySubject(subjectId = subjectId)
            .map { it.id }

        collectionRepository.updateAll(CollectionsUpdateCommand.RemoveSubjectFromAllCollections(subjectId = subjectId))

        collectionSearchService.upsert(collectionRepository.findAll(idsOfCollectionsContainingSubject).asSequence())
    }
}

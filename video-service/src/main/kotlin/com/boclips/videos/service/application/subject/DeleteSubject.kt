package com.boclips.videos.service.application.subject

import com.boclips.videos.service.domain.model.collection.CollectionFilter
import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.collection.CollectionIndex
import com.boclips.videos.service.domain.service.subject.SubjectRepository

class DeleteSubject(
    private val subjectRepository: SubjectRepository,
    private val collectionRepository: CollectionRepository,
    private val collectionIndex: CollectionIndex
) {
    operator fun invoke(subjectId: SubjectId, user: User) {
        subjectRepository.delete(subjectId)

        collectionRepository.streamUpdate(CollectionFilter.HasSubjectId(subjectId), { collection ->
            CollectionUpdateCommand.RemoveSubjectFromCollection(
                collectionId = collection.id,
                subjectId = subjectId,
                user = user
            )
        }, { updateResult ->
            collectionIndex.upsert(sequenceOf(updateResult.collection))
        })
    }
}

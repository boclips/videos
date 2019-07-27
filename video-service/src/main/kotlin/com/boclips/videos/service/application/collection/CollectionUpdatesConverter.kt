package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.subject.SubjectRepository
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest

class CollectionUpdatesConverter(val subjectRepository: SubjectRepository) {
    fun convert(updateCollectionRequest: UpdateCollectionRequest?): List<CollectionUpdateCommand> {
        updateCollectionRequest ?: return emptyList()

        return listOfNotNull(
            updateCollectionRequest.title?.let { CollectionUpdateCommand.RenameCollection(title = it) },
            updateCollectionRequest.isPublic?.let { CollectionUpdateCommand.ChangeVisibility(isPublic = it) },
            updateCollectionRequest.subjects?.let {
                CollectionUpdateCommand.ReplaceSubjects(subjects = it.map { subjectId ->
                    subjectRepository.findById(SubjectId(value = subjectId))!!
                }.toSet())
            },
            updateCollectionRequest.ageRange?.let { ageRange ->
                ageRange.min?.let { min ->
                    CollectionUpdateCommand.ChangeAgeRange(min, ageRange.max)
                }
            }
        )
    }
}

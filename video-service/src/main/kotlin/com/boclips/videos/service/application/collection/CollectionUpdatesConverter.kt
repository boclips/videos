package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest

class CollectionUpdatesConverter {
    companion object {
        fun convert(updateCollectionRequest: UpdateCollectionRequest?): List<CollectionUpdateCommand> {
            updateCollectionRequest ?: return emptyList()

            return listOfNotNull(
                buildRenameTitleCommand(updateCollectionRequest),
                buildChangeVisibilityCommand(updateCollectionRequest),
                buildReplaceSubjectCommand(updateCollectionRequest),
                buildChangeAgeRangeCommand(updateCollectionRequest)
            )
        }

        private fun buildRenameTitleCommand(updateCollectionRequest: UpdateCollectionRequest) =
            updateCollectionRequest.title?.let { CollectionUpdateCommand.RenameCollection(title = it) }

        private fun buildChangeVisibilityCommand(updateCollectionRequest: UpdateCollectionRequest) =
            updateCollectionRequest.isPublic?.let { CollectionUpdateCommand.ChangeVisibility(isPublic = it) }

        private fun buildReplaceSubjectCommand(updateCollectionRequest: UpdateCollectionRequest) =
            updateCollectionRequest.subjects?.let {
                CollectionUpdateCommand.ReplaceSubjects(subjects = it.map { subjectId ->
                    SubjectId(subjectId)
                }.toSet())
            }

        private fun buildChangeAgeRangeCommand(updateCollectionRequest: UpdateCollectionRequest) =
            updateCollectionRequest.ageRange?.let { ageRange ->
                ageRange.min?.let { min ->
                    CollectionUpdateCommand.ChangeAgeRange(min, ageRange.max)
                }

            }
    }
}

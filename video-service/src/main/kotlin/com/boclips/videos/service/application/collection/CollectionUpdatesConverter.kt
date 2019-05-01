package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.SubjectId
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
            updateCollectionRequest.title?.let { CollectionUpdateCommand.RenameCollectionCommand(title = it) }

        private fun buildChangeVisibilityCommand(updateCollectionRequest: UpdateCollectionRequest) =
            updateCollectionRequest.isPublic?.let { CollectionUpdateCommand.ChangeVisibilityCommand(isPublic = it) }

        private fun buildReplaceSubjectCommand(updateCollectionRequest: UpdateCollectionRequest) =
            updateCollectionRequest.subjects?.let {
                CollectionUpdateCommand.ReplaceSubjectsCommand(subjects = it.map { subjectId ->
                    SubjectId(subjectId)
                }.toSet())
            }

        private fun buildChangeAgeRangeCommand(updateCollectionRequest: UpdateCollectionRequest) =
            updateCollectionRequest.ageRange?.let {
                val minAge: Int
                var maxAge: Int? = null

                if (it.contains('+')) {
                    minAge = it.split('+')[0].toInt()
                } else {
                    val split = it.split("-")
                    minAge = Math.min(split[0].toInt(), split[1].toInt())
                    maxAge = Math.max(split[0].toInt(), split[1].toInt())
                }

                CollectionUpdateCommand.ChangeAgeRangeCommand(minAge, maxAge)
            }
    }
}
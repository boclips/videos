package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.collection.SubjectId
import com.boclips.videos.service.domain.model.video.VideoId

sealed class CollectionUpdateCommand {
    data class AddVideoToCollectionCommand(val videoId: VideoId) : CollectionUpdateCommand()
    data class RemoveVideoFromCollectionCommand(val videoId: VideoId) : CollectionUpdateCommand()
    data class RenameCollectionCommand(val title: String) : CollectionUpdateCommand()
    data class ChangeVisibilityCommand(val isPublic: Boolean) : CollectionUpdateCommand()
    data class ReplaceSubjectsCommand(val subjects: Set<SubjectId>) : CollectionUpdateCommand()
    data class ChangeAgeRangeCommand(val minAge: Int, val maxAge: Int?) : CollectionUpdateCommand()
}


package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.SubjectId
import com.boclips.videos.service.domain.model.asset.AssetId

sealed class CollectionUpdateCommand {
    data class AddVideoToCollectionCommand(val videoId: AssetId) : CollectionUpdateCommand()
    data class RemoveVideoFromCollectionCommand(val videoId: AssetId) : CollectionUpdateCommand()
    data class RenameCollectionCommand(val title: String) : CollectionUpdateCommand()
    data class ChangeVisibilityCommand(val isPublic: Boolean) : CollectionUpdateCommand()
    data class ReplaceSubjectsCommand(val subjects: Set<SubjectId>) : CollectionUpdateCommand()
}


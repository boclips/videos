package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest

class CollectionUpdatesConverter {
    companion object {
        fun convert(updateCollectionRequest: UpdateCollectionRequest?): List<CollectionUpdateCommand> {
            updateCollectionRequest ?: return emptyList()

            val updates = mutableListOf<CollectionUpdateCommand>()

            if (updateCollectionRequest.title != null) {
                updates.add(CollectionUpdateCommand.RenameCollectionCommand(title = updateCollectionRequest.title!!))
            }

            if (updateCollectionRequest.isPublic !== null) {
                updates.add(CollectionUpdateCommand.ChangeVisibilityCommand(isPublic = updateCollectionRequest.isPublic!!))
            }

            return updates
        }
    }
}
package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.collection.RenameCollectionCommand
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest

class CollectionUpdatesConverter {
    companion object {
        fun convert(updateCollectionRequest: UpdateCollectionRequest?): List<CollectionUpdateCommand> {
            updateCollectionRequest ?: return emptyList()

            if (updateCollectionRequest.title != null) {
                return listOf(
                    RenameCollectionCommand(
                        title = getOrThrow(
                            updateCollectionRequest.title,
                            "collection title"
                        )
                    )
                )
            }

            return emptyList()
        }
    }
}
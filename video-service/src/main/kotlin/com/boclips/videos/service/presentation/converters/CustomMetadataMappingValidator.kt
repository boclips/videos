package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.presentation.RowValidationError
import com.boclips.videos.service.presentation.VideoIdMetadataDoesntExist

object CustomMetadataMappingValidator {
    fun validateMapping(
        index: Int,
        item: RawCustomMappingMetadata,
        confirmedVideoIds: List<String>
    ): RowValidationError? {
        return if (!item.videoId.isNullOrEmpty() && !confirmedVideoIds.contains(
                item.videoId
            )
        ) {
            VideoIdMetadataDoesntExist(
                rowIndex = index,
                videoId = item.videoId
            )
        } else {
            null
        }
    }
}

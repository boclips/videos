package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.presentation.InvalidCategoryCode
import com.boclips.videos.service.presentation.MissingVideoId
import com.boclips.videos.service.presentation.VideoDoesntExist
import com.boclips.videos.service.presentation.VideoTaggingValidationError

object CategoryMappingValidator {

    fun validateMapping(
        index: Int,
        item: RawCategoryMappingMetadata,
        categoryCodes: List<String>,
        confirmedVideoIds: List<String>
    ): VideoTaggingValidationError? =
        if (item.videoId.isNullOrEmpty()) {
            MissingVideoId(rowIndex = index)
        } else if (!item.categoryCode.isNullOrEmpty() && !categoryCodes.contains(item.categoryCode)) {
            InvalidCategoryCode(code = item.categoryCode, rowIndex = index)
        } else if (!confirmedVideoIds.contains(item.videoId)) {
            VideoDoesntExist(rowIndex = index, videoId = item.videoId)
        } else {
            null
        }
}

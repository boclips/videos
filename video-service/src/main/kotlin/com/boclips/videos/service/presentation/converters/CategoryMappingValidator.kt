package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.presentation.InvalidCategoryCode
import com.boclips.videos.service.presentation.InvalidVideoId
import com.boclips.videos.service.presentation.MissingVideoId
import com.boclips.videos.service.presentation.VideoTaggingValidationError
import org.bson.types.ObjectId

object CategoryMappingValidator {

    fun validateMapping(
        index: Int,
        item: RawCategoryMappingMetadata,
        categoryCodes: List<String>
    ): VideoTaggingValidationError? =
        if (item.videoId.isNullOrEmpty()) {
            MissingVideoId(rowIndex = index)
        } else if (!ObjectId.isValid(item.videoId)) {
            InvalidVideoId(rowIndex = index, invalidId = item.videoId)
        } else if (!item.categoryCode.isNullOrEmpty() && !categoryCodes.contains(item.categoryCode)) {
            InvalidCategoryCode(code = item.categoryCode, rowIndex = index)
        } else {
            null
        }
}

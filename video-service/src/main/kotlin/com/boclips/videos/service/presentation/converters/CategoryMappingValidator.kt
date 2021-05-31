package com.boclips.videos.service.presentation.converters

import org.bson.types.ObjectId

object CategoryMappingValidator {

    fun validateMapping(
        index: Int,
        item: CategoryMappingMetadata,
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

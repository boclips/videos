package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.presentation.InvalidCategoryCode
import com.boclips.videos.service.presentation.InvalidPedagogyTags
import com.boclips.videos.service.presentation.VideoDoesntExist
import com.boclips.videos.service.presentation.VideoTaggingValidationError

object CategoryMappingValidator {

    fun validateMapping(
        index: Int,
        item: RawCategoryMappingMetadata,
        categoryCodes: List<String>,
        confirmedVideoIds: List<String>,
        confirmedTags: List<String>
    ): VideoTaggingValidationError? =
        if (!item.categoryCode.isNullOrEmpty() && !categoryCodes.contains(item.categoryCode)) {
            InvalidCategoryCode(code = item.categoryCode, rowIndex = index)
        } else if (!item.videoId.isNullOrEmpty() && !confirmedVideoIds.contains(item.videoId)) {
            VideoDoesntExist(rowIndex = index, videoId = item.videoId)
        } else if (!item.tag.isNullOrEmpty() && !confirmedTags.contains(item.tag)) {
            InvalidPedagogyTags(rowIndex = index, tag = item.tag)
        } else {
            null
        }
}

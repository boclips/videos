package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.domain.model.AccessValidationResult

data class FindCollectionResult(val collection: Collection?, val accessValidationResult: AccessValidationResult) {
    companion object {
        fun success(collection: Collection) =
            FindCollectionResult(collection = collection, accessValidationResult = AccessValidationResult.SUCCESS)

        fun error(accessValidationResult: AccessValidationResult) =
            FindCollectionResult(collection = null, accessValidationResult = accessValidationResult)
    }
}

package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.AccessError
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionReadService

class GetCollection(
    private val collectionReadService: CollectionReadService
) {
    operator fun invoke(
        collectionId: String,
        user: User,
        referer: String? = null,
        shareCode: String? = null
    ): Collection {
        val findCollectionResult =
            collectionReadService.find(CollectionId(value = collectionId), user, referer, shareCode)

        if (findCollectionResult.collection != null) {
            return findCollectionResult.collection
        }

        when (findCollectionResult.accessValidationResult.error) {
            is AccessError.InvalidShareCode -> throw OperationForbiddenException()
            else -> throw CollectionNotFoundException(collectionId)
        }
    }
}

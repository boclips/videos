package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.AccessError
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.FindCollectionResult
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.collection.CollectionRetrievalService

class GetCollection(private val collectionRetrievalService: CollectionRetrievalService) {
    operator fun invoke(
        collectionId: String,
        user: User,
        referer: String? = null,
        shareCode: String? = null
    ): Collection {
        val findCollectionResult: FindCollectionResult =
            collectionRetrievalService.find(
                id = CollectionId(value = collectionId),
                user = user,
                referer = referer,
                shareCode = shareCode
            )

        if (findCollectionResult.collection != null) {
            return findCollectionResult.collection
        }

        when (findCollectionResult.accessValidationResult.error) {
            is AccessError.InvalidShareCode -> throw OperationForbiddenException()
            else -> throw CollectionNotFoundException(collectionId)
        }
    }
}

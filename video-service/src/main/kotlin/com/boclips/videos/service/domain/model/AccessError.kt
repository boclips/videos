package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.collection.CollectionId

sealed class AccessError(val message: String, val collectionId: CollectionId, val userId: UserId) {
    class Default(collectionId: CollectionId, userId: UserId) :
        AccessError(
            message = "User ${userId.value} does not have access to collection ${collectionId.value}",
            collectionId = collectionId,
            userId = userId
        )

    class InvalidShareCode(collectionId: CollectionId, userId: UserId, shareCode: String?, referer: String?) :
        AccessError(
            message = "User ${userId.value} cannot access collection ${collectionId.value} with: shareCode = $shareCode referer = $referer",
            collectionId = collectionId,
            userId = userId
        )
}

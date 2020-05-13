package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.collection.CollectionRetrievalService
import com.boclips.videos.service.domain.service.user.UserService

class GetCollection(
    private val collectionRetrievalService: CollectionRetrievalService,
    private val userService: UserService
) {
    operator fun invoke(
        collectionId: String,
        user: User,
        referer: String? = null,
        shareCode: String? = null
    ): Collection {
        val collection =
            collectionRetrievalService.findAnyCollection(id = CollectionId(value = collectionId), user = user)

        return when (collection) {
            null -> throw CollectionNotFoundException(collectionId)
            else -> {
                if (!user.isAuthenticated) {
                    if (referer == null || shareCode == null) {
                        throw OperationForbiddenException()
                    }

                    if (!userService.isShareCodeValid(referer, shareCode)) {
                        throw OperationForbiddenException()
                    }
                }

                collection
            }
        }
    }
}

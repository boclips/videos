package com.boclips.videos.service.application.collection

import com.boclips.users.client.UserServiceClient
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionReadService

class GetCollection(
    private val collectionReadService: CollectionReadService,
    private val userServiceClient: UserServiceClient
) {
    operator fun invoke(
        collectionId: String,
        user: User,
        referer: String? = null,
        shareCode: String? = null
    ): Collection {
        if ((user.isAuthenticated && user.isPermittedToViewCollections)
            || userServiceClient.validateShareCode(referer, shareCode)
        ) {
            return collectionReadService.find(CollectionId(value = collectionId), user)
                ?: throw CollectionNotFoundException(collectionId)
        } else {
            throw OperationForbiddenException("Unauthenticated users must provide a valid share code and referer id")
        }
    }
}

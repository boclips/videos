package com.boclips.videos.service.domain.service.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.application.getCurrentUser
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.service.AccessRuleService

class CollectionAccessService(
    private val collectionRepository: CollectionRepository,
    private val accessRuleService: AccessRuleService
) {
    fun hasWriteAccess(collectionId: String): Boolean =
        getCollectionOrThrow(collectionId = collectionId, isForReading = false) != null

    fun hasReadAccess(collectionId: String): Boolean =
        getCollectionOrThrow(collectionId = collectionId, isForReading = true) != null

    private fun getCollectionOrThrow(collectionId: String, isForReading: Boolean): Collection? {
        val user = getCurrentUser()
        val collection = collectionRepository.find(CollectionId(collectionId))
            ?: throw CollectionNotFoundException(collectionId)
        val accessRules = accessRuleService.getRules(user)

        return when {
            isForReading && collection.isPublic -> collection
            isForReading && accessRules.allowsAccessTo(collection) -> collection
            collection.owner == UserId(user.id) -> collection
            UserExtractor.currentUserHasRole(UserRoles.VIEW_ANY_COLLECTION) -> collection
            else -> null
        }
    }
}

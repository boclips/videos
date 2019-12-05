package com.boclips.videos.service.domain.service.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.application.getCurrentUser
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.service.AccessRuleService

class CollectionAccessService(private val accessRuleService: AccessRuleService) {
    fun hasWriteAccess(collection: Collection): Boolean =
        hasAccess(collection = collection, readOnly = false)

    fun hasReadAccess(collection: Collection): Boolean =
        hasAccess(collection = collection, readOnly = true)

    private fun hasAccess(collection: Collection, readOnly: Boolean): Boolean {
        val user = getCurrentUser()
        val accessRules = accessRuleService.getRules(user)

        return when {
            readOnly && collection.isPublic -> true
            readOnly && accessRules.allowsAccessTo(collection) -> true
            collection.owner == UserId(user.id) -> true
            UserExtractor.currentUserHasRole(UserRoles.VIEW_ANY_COLLECTION) -> true
            else -> false
        }
    }
}

package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.AccessRuleService

class CollectionAccessService(private val accessRuleService: AccessRuleService) {
    fun hasWriteAccess(collection: Collection, user: User): Boolean =
        hasAccess(collection = collection, readOnly = false, user = user)

    fun hasReadAccess(collection: Collection, user: User): Boolean =
        hasAccess(collection = collection, readOnly = true, user = user)

    private fun hasAccess(collection: Collection, readOnly: Boolean, user: User): Boolean {
        val accessRules = accessRuleService.getRules(user)

        return when {
            readOnly && collection.isPublic -> true
            readOnly && accessRules.allowsAccessTo(collection) -> true
            collection.owner == user.id -> true
            user.isPermittedToViewAnyCollection -> true
            else -> false
        }
    }
}

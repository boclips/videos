package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.user.User

class CollectionAccessService {
    fun hasWriteAccess(collection: Collection, user: User): Boolean {
        return hasAccess(collection = collection, readOnly = false, user = user)
    }

    fun hasReadAccess(collection: Collection, user: User): Boolean {
        return hasAccess(collection = collection, readOnly = true, user = user)
    }

    // TODO: These rules are dubious, defy the point of access rules
    private fun hasAccess(
        collection: Collection,
        readOnly: Boolean,
        user: User
    ): Boolean {
        return when {
            collection.owner == user.id -> true
            user.isPermittedToViewAnyCollection -> true
            readOnly && user.accessRules.collectionAccess.allowsAccessTo(collection) -> true
            readOnly && user.isAuthenticated -> true // this should be removed with comprehensive use of access rules
            else -> false
        }
    }
}

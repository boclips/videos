package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.AccessError
import com.boclips.videos.service.domain.model.AccessValidationResult
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.user.UserService

class CollectionAccessService(
    private val userService: UserService
) {
    fun hasWriteAccess(collection: Collection, user: User): Boolean =
        validateAccess(collection = collection, readOnly = false, user = user).successful

    fun validateReadAccess(
        collection: Collection,
        user: User,
        referer: String?,
        shareCode: String?
    ): AccessValidationResult =
        when (user.isAuthenticated) {
            true -> validateReadAccessForAuthenticated(
                collection = collection,
                referer = referer,
                shareCode = shareCode,
                user = user
            )
            false -> validateReadAccessForAnonymous(
                collection = collection,
                referer = referer,
                shareCode = shareCode,
                user = user
            )
        }

    private fun validateReadAccessForAnonymous(
        collection: Collection,
        user: User,
        referer: String?,
        shareCode: String?
    ): AccessValidationResult = validateShareCodeForCollection(
        collection = collection,
        referer = referer,
        shareCode = shareCode,
        user = user
    )

    private fun validateReadAccessForAuthenticated(
        collection: Collection,
        user: User,
        referer: String?,
        shareCode: String?
    ): AccessValidationResult {
        val validationResult = validateAccess(collection = collection, readOnly = true, user = user)
        return if (validationResult.successful) {
            validationResult
        } else {
            validateShareCodeForCollection(
                collection = collection,
                referer = referer,
                shareCode = shareCode,
                user = user
            )
        }
    }

    private fun validateAccess(
        collection: Collection,
        readOnly: Boolean,
        user: User
    ): AccessValidationResult {
        return when {
            readOnly && collection.isPublic && user.isAuthenticated && user.isPermittedToViewCollections -> AccessValidationResult.SUCCESS
            readOnly && user.accessRules.collectionAccess.allowsAccessTo(collection) -> AccessValidationResult.SUCCESS
            collection.owner == user.id -> AccessValidationResult.SUCCESS
            user.isPermittedToViewAnyCollection -> AccessValidationResult.SUCCESS
            else -> AccessValidationResult(
                error = AccessError.Default(collectionId = collection.id, userId = user.id)
            )
        }
    }

    private fun validateShareCodeForCollection(
        collection: Collection,
        referer: String?,
        shareCode: String?,
        user: User
    ): AccessValidationResult {
        if (shareCode == null || referer == null) {
            return AccessValidationResult(
                error = AccessError.InvalidShareCode(
                    collectionId = collection.id,
                    userId = user.id,
                    shareCode = shareCode,
                    referer = referer
                )
            )
        }

        val shareCodeIsValid = userService.isShareCodeValid(referer, shareCode)

        val ownerIsReferer = collection.owner.value == referer
        return when {
            collection.isPublic && shareCodeIsValid -> AccessValidationResult.SUCCESS
            ownerIsReferer && shareCodeIsValid -> AccessValidationResult.SUCCESS
            else -> AccessValidationResult(
                error = AccessError.InvalidShareCode(
                    collectionId = collection.id,
                    userId = user.id,
                    shareCode = shareCode,
                    referer = referer
                )
            )
        }
    }
}

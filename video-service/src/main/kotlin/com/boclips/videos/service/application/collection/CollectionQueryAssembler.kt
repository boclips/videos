package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.security.utils.User
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
import com.boclips.videos.service.domain.service.CollectionAccessRule

class CollectionQueryAssembler {
    fun assemble(
        filter: CollectionFilter, user: User?,
        collectionAccess: CollectionAccessRule = CollectionAccessRule.Unspecified
    ): CollectionSearchQuery {
        val query = CollectionSearchQuery(
            text = filter.query,
            subjectIds = filter.subjects,
            owner = filter.owner,
            bookmarkedBy = if (filter.visibility == CollectionFilter.Visibility.BOOKMARKED) user?.id else null,
            visibility = when (filter.visibility) {
                CollectionFilter.Visibility.PUBLIC -> listOf(CollectionVisibility.PUBLIC)
                CollectionFilter.Visibility.PRIVATE -> listOf(CollectionVisibility.PRIVATE)
                CollectionFilter.Visibility.BOOKMARKED -> listOf(CollectionVisibility.PUBLIC)
                CollectionFilter.Visibility.ALL -> listOf(CollectionVisibility.PRIVATE, CollectionVisibility.PUBLIC)
            },
            pageSize = filter.pageSize,
            pageIndex = filter.pageNumber,
            permittedCollections = when (collectionAccess) {
                is CollectionAccessRule.RestrictedTo -> collectionAccess.collectionIds.toList()
                is CollectionAccessRule.Unspecified -> null
            }
        )

        val involvesPrivateCollections =
            filter.visibility == CollectionFilter.Visibility.PRIVATE ||
                filter.visibility == CollectionFilter.Visibility.ALL

        user?.let { authenticatedUser ->
            return when {
                authenticatedUser.hasRole(UserRoles.VIEW_ANY_COLLECTION) -> query
                involvesPrivateCollections -> {
                    if (query.permittedCollections != null) {
                        return query
                    }

                    val owner = filter.owner
                        ?: throw OperationForbiddenException("owner must be specified for private collections access")

                    if (owner == authenticatedUser.id) {
                        return query
                    }

                    throw OperationForbiddenException("${authenticatedUser.id} is not authorized to access $owner")
                }
                else -> query
            }
        }

            ?: throw OperationForbiddenException("User must be authenticated to access collections")
    }
}

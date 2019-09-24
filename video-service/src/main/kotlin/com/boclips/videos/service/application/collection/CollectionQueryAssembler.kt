package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.security.utils.User
import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery

class CollectionQueryAssembler {
    fun assemble(filter: CollectionFilter, user: User?): CollectionSearchQuery {
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
            pageIndex = filter.pageNumber
        )

        user?.let { existingUser ->
            return when {
                existingUser.hasRole(UserRoles.VIEW_ANY_COLLECTION) -> query
                filter.visibility == CollectionFilter.Visibility.PRIVATE -> {
                    val owner = filter.owner
                        ?: throw OperationForbiddenException("owner must be specified for private collections access")
                    val authenticatedUserId = existingUser.id

                    if (owner == authenticatedUserId || UserExtractor.currentUserHasRole(UserRoles.VIEW_ANY_COLLECTION)) {
                        return query
                    }

                    throw OperationForbiddenException("$authenticatedUserId is not authorized to access $owner")
                }
                else -> {
                    if (filter.visibility == CollectionFilter.Visibility.PRIVATE
                        || filter.visibility == CollectionFilter.Visibility.ALL
                    ) {
                        throw OperationForbiddenException()
                    }

                    query
                }
            }
        }

            ?: throw OperationForbiddenException("User must be authenticated to access collections")
    }
}

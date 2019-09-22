package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.security.utils.User
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery

class CollectionQueryAssembler {
    fun assemble(filter: CollectionFilter, user: User?): CollectionSearchQuery {
        val query = CollectionSearchQuery(
            text = filter.query,
            subjectIds = filter.subjects,
            visibility = when (filter.visibility) {
                CollectionFilter.Visibility.PUBLIC -> listOf(CollectionVisibility.PUBLIC)
                CollectionFilter.Visibility.PRIVATE -> listOf(CollectionVisibility.PRIVATE)
                CollectionFilter.Visibility.BOOKMARKED -> listOf(CollectionVisibility.PUBLIC)
                CollectionFilter.Visibility.ALL -> listOf(CollectionVisibility.PRIVATE, CollectionVisibility.PUBLIC)
            },
            pageSize = filter.pageSize,
            pageIndex = filter.pageNumber
        )

        user?.let {
            return if (it.hasRole(UserRoles.VIEW_ANY_COLLECTION)) {
                query
            } else {
                if (filter.visibility == CollectionFilter.Visibility.PRIVATE
                    || filter.visibility == CollectionFilter.Visibility.ALL
                ) {
                    throw OperationForbiddenException()
                }

                query
            }
        }

        throw OperationForbiddenException("User must be authenticated to access collections")
    }
}
package com.boclips.videos.service.domain.service.collection

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.infrastructure.convertPageToIndex
import mu.KLogging

class CollectionService(
    private val collectionRepository: CollectionRepository,
    private val collectionSearchService: CollectionSearchService,
    private val collectionAccessService: CollectionAccessService
) {
    companion object : KLogging()

    fun search(query: CollectionSearchQuery): Page<Collection> {
        val searchRequest = PaginatedSearchRequest(
            query = query.toSearchQuery(),
            startIndex = convertPageToIndex(query.pageSize, query.pageIndex),
            windowSize = query.pageSize
        )
        val collectionIds = collectionSearchService.search(searchRequest).map { CollectionId(value = it) }
        val collections = collectionRepository.findAll(collectionIds)

        logger.info { "Returning ${collections.size} collections for query $query" }

        val count = count(query)
        return Page(
            collections, PageInfo(
                hasMoreElements = count > query.pageIndexUpperBound(),
                totalElements = count,
                pageRequest = PageRequest(
                    page = query.pageIndex,
                    size = query.pageSize
                )
            )
        )
    }

    fun count(collectionSearchQuery: CollectionSearchQuery): Long {
        logger.info { "Counted collections for query $collectionSearchQuery" }
        return collectionSearchService.count(collectionSearchQuery.toSearchQuery())
    }

    fun find(id: CollectionId, user: User): Collection? =
        findIf(id, user, ::hasReadAccess)

    fun findWritable(id: CollectionId, user: User): Collection? =
        findIf(id, user, ::hasWriteAccess)

    private fun findIf(
        id: CollectionId,
        user: User,
        condition: (Collection, User) -> Boolean
    ): Collection? {
        val videoAccess = user.accessRules.videoAccess

        return collectionRepository.find(id)
            ?.takeIf { condition(it, user) }
            ?.let { collection ->
                collection.copy(
                    videos = when (videoAccess) {
                        VideoAccessRule.Everything ->
                            collection.videos
                        is VideoAccessRule.SpecificIds ->
                            collection.videos.intersect(videoAccess.videoIds).toList()
                    }
                )
            }
    }

    private fun hasWriteAccess(
        collection: Collection,
        user: User
    ): Boolean =
        collectionAccessService.hasWriteAccess(collection, user).also {
            if (!it) {
                logger.info {
                    "User ${user.id} does not have write access to Collection ${collection.id}"
                }
            }
        }

    private fun hasReadAccess(
        collection: Collection,
        user: User
    ): Boolean =
        collectionAccessService.hasReadAccess(collection, user).also {
            if (!it) {
                logger.info {
                    "User ${user.id} does not have read access to Collection ${collection.id}"
                }
            }
        }
}
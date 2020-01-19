package com.boclips.videos.service.domain.service.collection

import com.boclips.eventbus.domain.ResourceType
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
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.infrastructure.convertPageToIndex
import mu.KLogging

class CollectionReadService(
    private val collectionRepository: CollectionRepository,
    private val collectionSearchService: CollectionSearchService,
    private val collectionAccessService: CollectionAccessService,
    private val eventService: EventService
) {
    companion object : KLogging()

    fun search(query: CollectionSearchQuery, user: User): Page<Collection> {

        val accessRules = user.accessRules
        val searchRequest = PaginatedSearchRequest(
            query = query.toSearchQuery(),
            startIndex = convertPageToIndex(query.pageSize, query.pageIndex),
            windowSize = query.pageSize
        )
        val collectionIds = collectionSearchService.search(searchRequest).map { CollectionId(value = it) }
        val collections = collectionRepository.findAll(collectionIds).map {
            withPermittedVideos(it, accessRules.videoAccess)
        }

        eventService.saveResourcesSearched(
            resourceType = ResourceType.COLLECTION,
            query = query.toSearchQuery().phrase,
            pageIndex = query.pageIndex,
            pageSize = query.pageSize,
            totalResults = collectionIds.size.toLong(),
            pageResourceIds = collectionIds.map { collectionId -> collectionId.value },
            user = user
        )

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
                withPermittedVideos(collection, videoAccess)
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

    private fun withPermittedVideos(collection: Collection, videoAccessRule: VideoAccessRule): Collection =
        when (videoAccessRule) {
            is VideoAccessRule.SpecificIds -> collection.copy(videos = collection.videos.intersect(videoAccessRule.videoIds).toList())
            VideoAccessRule.Everything -> collection
        }
}

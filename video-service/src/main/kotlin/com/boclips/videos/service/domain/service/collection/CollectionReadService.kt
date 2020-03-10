package com.boclips.videos.service.domain.service.collection

import com.boclips.eventbus.domain.ResourceType
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.AccessError
import com.boclips.videos.service.domain.model.AccessValidationResult
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
import com.boclips.videos.service.domain.model.collection.FindCollectionResult
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.infrastructure.convertPageToIndex
import mu.KLogging

class CollectionReadService(
    private val collectionRepository: CollectionRepository,
    private val collectionSearchService: CollectionSearchService,
    private val collectionAccessService: CollectionAccessService,
    private val eventService: EventService,
    private val videoService: VideoService
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

    fun find(id: CollectionId, user: User, referer: String? = null, shareCode: String? = null): FindCollectionResult =
        collectionRepository.find(id)?.let {
            val accessValidationResult = validateReadAccess(it, user, referer, shareCode)
            return if (accessValidationResult.successful) {
                var collection = loadWithAccessibleVideos(it, user)
                collection = loadWithAccessibleAttachments(collection, user, referer, shareCode)

                FindCollectionResult.success(collection = collection)
            } else {
                FindCollectionResult.error(accessValidationResult = accessValidationResult)
            }
        } ?: FindCollectionResult.error(
            accessValidationResult = AccessValidationResult(
                false,
                error = AccessError.Default(id, user.id)
            )
        )

    fun findWritable(id: CollectionId, user: User): Collection? =
        collectionRepository.find(id)?.let {
            if (!hasWriteAccess(it, user)) {
                null
            } else {
                loadWithAccessibleVideos(it, user)
            }
        }

    private fun loadWithAccessibleAttachments(
        collection: Collection,
        user: User,
        referer: String?,
        shareCode: String?
    ): Collection {
        return if (!user.isAuthenticated && shareCode != null && referer != null) {
            collection.copy(attachments = emptySet())
        } else {
            collection
        }
    }

    private fun loadWithAccessibleVideos(
        collection: Collection,
        user: User
    ): Collection {
        val videoAccess = user.accessRules.videoAccess
        return withPermittedVideos(collection, videoAccess)
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

    private fun validateReadAccess(
        collection: Collection,
        user: User,
        referer: String? = null,
        shareCode: String? = null
    ): AccessValidationResult =
        collectionAccessService.validateReadAccess(collection, user, referer, shareCode).also {
            if (!it.successful) {
                logger.info {
                    "User ${user.id} does not have read access to Collection ${collection.id}"
                }
            }
        }

    private fun withPermittedVideos(collection: Collection, videoAccess: VideoAccess): Collection =
        videoService.getPlayableVideos(
            videoIds = collection.videos,
            videoAccess = videoAccess
        )
            .map { it.videoId }
            .let { collection.copy(videos = it) }
}

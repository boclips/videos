package com.boclips.videos.service.domain.service.collection

import com.boclips.eventbus.domain.ResourceType
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.videos.service.application.video.VideoRetrievalService
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.common.ResultsPage
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.infrastructure.convertPageToIndex
import mu.KLogging

class CollectionRetrievalService(
    private val collectionRepository: CollectionRepository,
    private val collectionIndex: CollectionIndex,
    private val collectionAccessService: CollectionAccessService,
    private val eventService: EventService,
    private val videoRetrievalService: VideoRetrievalService
) {
    companion object : KLogging()

    fun search(
        query: CollectionSearchQuery,
        user: User,
        queryParams: Map<String, List<String>> = emptyMap()
    ): ResultsPage<Collection, Nothing> {
        val searchQuery = query.toSearchQuery()

        val searchRequest = PaginatedIndexSearchRequest(
            query = searchQuery,
            startIndex = convertPageToIndex(query.pageSize, query.pageIndex),
            windowSize = query.pageSize
        )
        val results = collectionIndex.search(searchRequest)

        logger.info { "Found ${results.counts.totalHits} collections for query $searchRequest" }

        val accessRules = user.accessRules
        val collectionIds = results.elements.map { CollectionId(value = it) }
        val collections = collectionRepository.findAll(collectionIds).map { collections ->
            addVideosToCollection(collections, accessRules.videoAccess)
        }

        eventService.saveResourcesSearched(
            resourceType = ResourceType.COLLECTION,
            query = searchQuery.phrase,
            pageIndex = query.pageIndex,
            pageSize = query.pageSize,
            totalResults = collectionIds.size.toLong(),
            pageResourceIds = collectionIds.map { collectionId -> collectionId.value },
            queryParams = queryParams,
            user = user
        )

        logger.info { "Returning ${collections.size} collections for query $query" }

        val count = results.counts.totalHits
        return ResultsPage(
            elements = collections,
            pageInfo = PageInfo(
                hasMoreElements = count > query.pageIndexUpperBound(),
                totalElements = count,
                pageRequest = PageRequest(
                    page = query.pageIndex,
                    size = query.pageSize
                )
            )
        )
    }

    fun findAnyCollection(id: CollectionId, user: User, populateVideos: Boolean = true): Collection? {
        val collection = collectionRepository.find(id)

        return collection?.let {
            return when {
                !collectionAccessService.hasReadAccess(collection, user) -> null
                populateVideos -> addVideosToCollection(it, user.accessRules.videoAccess)
                else -> it.copy(videos = emptyList())
            }
        }
    }

    fun findOwnCollection(id: CollectionId, user: User): Collection? {
        return collectionRepository.find(id)?.let { collection ->
            if (!collectionAccessService.hasWriteAccess(collection, user).also {
                if (!it) {
                    logger.info {
                        "User ${user.id} does not have write access to Collection ${collection.id}"
                    }
                }
            }
            ) {
                null
            } else {
                addVideosToCollection(collection, user.accessRules.videoAccess)
            }
        }
    }

    private fun addVideosToCollection(collection: Collection, videoAccess: VideoAccess): Collection =
        videoRetrievalService.getPlayableVideos(
            videoIds = collection.videos,
            videoAccess = videoAccess
        )
            .map { it.videoId }
            .let { collection.copy(videos = it) }
}

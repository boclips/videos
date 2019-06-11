package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.video.exceptions.SearchRequestValidationException
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.SortKey
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository

class SearchVideo(
    private val getVideoById: GetVideoById,
    private val getAllVideosById: GetAllVideosById,
    private val getVideosByQuery: GetVideosByQuery,
    private val videoRepository: VideoRepository,
    private val getAllVideosByContentPartnerId: GetAllVideosByContentPartnerId
) {
    companion object {
        fun isAlias(potentialAlias: String): Boolean = Regex("\\d+").matches(potentialAlias)
    }

    fun byId(id: String?) = getVideoById(resolveToAssetId(id)!!)

    fun byIds(ids: List<String>) = getAllVideosById(ids.mapNotNull { this.resolveToAssetId(it, false) })

    fun byContentPartnerId(contentPartnerId: String) =
        getAllVideosByContentPartnerId(contentPartnerId = ContentPartnerId(contentPartnerId))

    fun byQuery(
        query: String?,
        sortBy: SortKey? = null,
        includeTags: List<String>,
        excludeTags: List<String>,
        minDuration: String? = null,
        maxDuration: String? = null,
        releasedDateFrom: String? = null,
        releasedDateTo: String? = null,
        pageSize: Int,
        pageNumber: Int,
        source: String? = null
    ) = getVideosByQuery(
        query = getOrThrow(query),
        sortBy = sortBy,
        includeTags = includeTags,
        excludeTags = excludeTags,
        minDurationString = minDuration,
        maxDurationString = maxDuration,
        releasedDateFrom = releasedDateFrom,
        releasedDateTo = releasedDateTo,
        pageSize = pageSize,
        pageNumber = pageNumber,
        source = source
    )

    private fun resolveToAssetId(videoIdParam: String?, throwIfDoesNotExist: Boolean = true): VideoId? {
        val videoId = getOrThrow(videoIdParam)

        return try {
            if (isAlias(videoId)) {
                videoRepository.resolveAlias(videoId) ?: throw VideoNotFoundException()
            } else {
                VideoId(value = videoId)
            }
        } catch (e: Exception) {
            if (throwIfDoesNotExist)
                throw e
            null
        }
    }

    private fun getOrThrow(videoId: String?): String {
        if (videoId == null) throw SearchRequestValidationException()
        return videoId
    }
}

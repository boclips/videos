package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.video.exceptions.SearchRequestValidationException
import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.SortKey
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository

class SearchVideo(
    private val getVideoById: GetVideoById,
    private val getAllVideosById: GetAllVideosById,
    private val getVideosByQuery: GetVideosByQuery,
    private val videoAssetRepository: VideoAssetRepository
) {
    companion object {
        fun isAlias(potentialAlias: String): Boolean = Regex("\\d+").matches(potentialAlias)
    }

    fun byId(id: String?) = getVideoById(resolveToAssetId(id)!!)

    fun byIds(ids: List<String>) = getAllVideosById(ids.mapNotNull { this.resolveToAssetId(it, false) })

    fun byQuery(
        query: String?,
        sortBy: SortKey? = null,
        includeTags: List<String>,
        excludeTags: List<String>,
        minDuration: String? = null,
        maxDuration: String? = null,
        pageSize: Int,
        pageNumber: Int
    ) = getVideosByQuery(getOrThrow(query), sortBy, includeTags, excludeTags, minDuration, maxDuration, pageSize, pageNumber)

    private fun resolveToAssetId(videoIdParam: String?, throwIfDoesNotExist: Boolean = true): AssetId? {
        val videoId = getOrThrow(videoIdParam)

        return try {
            if (isAlias(videoId)) {
                videoAssetRepository.resolveAlias(videoId) ?: throw VideoAssetNotFoundException()
            } else {
                AssetId(value = videoId)
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

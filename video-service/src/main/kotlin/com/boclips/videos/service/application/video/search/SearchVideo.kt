package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.video.exceptions.SearchRequestValidationException
import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
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

    fun byId(id: String?) = getVideoById(resolveToAssetId(id))

    fun byIds(ids: List<String>) = getAllVideosById(ids.map(this::resolveToAssetId))

    fun byQuery(
            query: String?,
            includeTags: List<String>,
            excludeTags: List<String>,
            pageSize: Int,
            pageNumber: Int
    ) = getVideosByQuery(getOrThrow(query), includeTags, excludeTags, pageSize, pageNumber)

    private fun resolveToAssetId(videoIdParam: String?): AssetId {
        val videoId = getOrThrow(videoIdParam)

        return if (isAlias(videoId)) {
            videoAssetRepository.resolveAlias(videoId) ?: throw VideoAssetNotFoundException()
        } else {
            AssetId(value = videoId)
        }
    }

    private fun getOrThrow(videoId: String?): String {
        if (videoId == null) throw SearchRequestValidationException()
        return videoId
    }
}
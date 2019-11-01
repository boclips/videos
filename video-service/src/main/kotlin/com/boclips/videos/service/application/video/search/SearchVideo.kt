package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.video.exceptions.SearchRequestValidationException
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.domain.model.video.IllegalVideoIdentifierException
import com.boclips.videos.service.domain.model.video.SortKey
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.web.exceptions.ResourceNotFoundApiException

class SearchVideo(
    private val getVideoById: GetVideoById,
    private val getAllVideosById: GetAllVideosById,
    private val getVideosByQuery: GetVideosByQuery,
    private val videoRepository: VideoRepository
) {
    companion object {
        fun isAlias(potentialAlias: String): Boolean = Regex("\\d+").matches(potentialAlias)
    }

    fun byId(id: String?): Video {
        return getVideoById(resolveToAssetId(id)!!)
    }

    fun byIds(ids: List<String>): List<Video> {
        return getAllVideosById(ids.mapNotNull { this.resolveToAssetId(it, false) })
    }

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
        source: String? = null,
        ageRangeMin: Int? = null,
        ageRangeMax: Int? = null,
        subjects: Set<String> = emptySet(),
        promoted: Boolean? = null
    ): Page<Video> {
        return getVideosByQuery(
            query = query ?: "",
            sortBy = sortBy,
            includeTags = includeTags,
            excludeTags = excludeTags,
            minDurationString = minDuration,
            maxDurationString = maxDuration,
            releasedDateFrom = releasedDateFrom,
            releasedDateTo = releasedDateTo,
            pageSize = pageSize,
            pageNumber = pageNumber,
            source = source,
            ageRangeMin = ageRangeMin,
            ageRangeMax = ageRangeMax,
            subjects = subjects,
            promoted = promoted
        )
    }

    private fun resolveToAssetId(videoIdParam: String?, throwIfDoesNotExist: Boolean = true): VideoId? {
        if (videoIdParam == null) throw SearchRequestValidationException()

        return try {
            if (isAlias(videoIdParam)) {
                videoRepository.resolveAlias(videoIdParam) ?: throw VideoNotFoundException()
            } else {
                VideoId(value = videoIdParam)
            }
        } catch (e: IllegalVideoIdentifierException) {
            if (throwIfDoesNotExist)
                throw ResourceNotFoundApiException("Video not found", e.message ?: "")
            null
        } catch (e: Exception) {
            if (throwIfDoesNotExist)
                throw e
            null
        }
    }
}

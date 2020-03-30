package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.video.exceptions.SearchRequestValidationException
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.common.ResultsPage
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.IllegalVideoIdentifierException
import com.boclips.videos.service.domain.model.video.request.SortKey
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoCounts
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.presentation.converters.convertAgeRanges
import com.boclips.web.exceptions.ResourceNotFoundApiException

class SearchVideo(
    private val getVideoById: GetVideoById,
    private val getVideosByQuery: GetVideosByQuery,
    private val getAllVideosById: GetAllVideosById,
    private val videoRepository: VideoRepository
) {
    companion object {
        fun isAlias(potentialAlias: String): Boolean = Regex("\\d+").matches(potentialAlias)
    }

    fun byId(id: String?, user: User): Video {

        return getVideoById(resolveToAssetId(id)!!, user)
    }

    fun byIds(ids: List<String>, user: User): List<Video> {
        return getAllVideosById(ids.mapNotNull { this.resolveToAssetId(it, false) }, user)
    }

    fun byQuery(
        query: String?,
        sortBy: SortKey? = null,
        bestFor: List<String>? = null,
        minDuration: String? = null,
        maxDuration: String? = null,
        duration: List<String>? = null,
        releasedDateFrom: String? = null,
        releasedDateTo: String? = null,
        pageSize: Int,
        pageNumber: Int,
        source: String? = null,
        ageRangeMin: Int? = null,
        ageRangeMax: Int? = null,
        ageRanges: List<String>? = null,
        ageRangeFacets: List<String>? = null,
        subjects: Set<String> = emptySet(),
        subjectsSetManually: Boolean? = null,
        promoted: Boolean? = null,
        contentPartnerNames: Set<String> = emptySet(),
        type: Set<String> = emptySet(),
        user: User
    ): ResultsPage<Video, VideoCounts> {
        return getVideosByQuery(
            query = query ?: "",
            sortBy = sortBy,
            bestFor = bestFor,
            minDurationString = minDuration,
            maxDurationString = maxDuration,
            duration = duration,
            releasedDateFrom = releasedDateFrom,
            releasedDateTo = releasedDateTo,
            pageSize = pageSize,
            pageNumber = pageNumber,
            source = source,
            ageRangeMin = ageRangeMin,
            ageRangeMax = ageRangeMax,
            ageRanges = ageRanges?.map(::convertAgeRanges) ?: emptyList(),
            ageRangesFacets = ageRangeFacets?.map(::convertAgeRanges),
            subjects = subjects,
            subjectsSetManually = subjectsSetManually,
            promoted = promoted,
            contentPartnerNames = contentPartnerNames,
            type = type,
            user = user
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

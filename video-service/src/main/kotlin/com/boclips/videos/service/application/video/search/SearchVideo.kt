package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.video.exceptions.SearchRequestValidationException
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.video.IllegalVideoIdentifierException
import com.boclips.videos.service.domain.model.video.SortKey
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.presentation.converters.convertAgeRanges
import com.boclips.web.exceptions.ResourceNotFoundApiException

class SearchVideo(
    private val getVideoById: GetVideoById,
    private val getVideosByQuery: GetVideosByQuery,
    private val videoRepository: VideoRepository
) {
    companion object {
        fun isAlias(potentialAlias: String): Boolean = Regex("\\d+").matches(potentialAlias)
    }

    fun byId(id: String?, user: User): Video {

        return getVideoById(resolveToAssetId(id)!!, user)
    }

    // TODO - forcing all video service look up  to go via ES means admin search no longer has access to all videos
    //  (due to hiding by distribution methods on the content partner level)
    // We should refactor this once access rules are powerful enough to include and exclude content partners
    @Deprecated("This will start using access rules once these support Content Partners.")
    fun byIds(ids: List<String>): List<Video> {
        return videoRepository.findAll(ids.mapNotNull { this.resolveToAssetId(it, false) })
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
        subjects: Set<String> = emptySet(),
        subjectsSetManually: Boolean? = null,
        promoted: Boolean? = null,
        contentPartnerNames: Set<String> = emptySet(),
        type: Set<String> = emptySet(),
        isClassroom: Boolean? = null,
        user: User
    ): Page<Video> {
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
            subjects = subjects,
            subjectsSetManually = subjectsSetManually,
            promoted = promoted,
            contentPartnerNames = contentPartnerNames,
            type = type,
            user = user,
            isClassroom = isClassroom
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

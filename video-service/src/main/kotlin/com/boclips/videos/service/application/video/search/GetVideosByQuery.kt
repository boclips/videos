package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.video.SortKey
import com.boclips.videos.service.domain.model.video.SubjectsRequest
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoRequest
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.VideoController.Companion.MAX_PAGE_SIZE
import mu.KLogging

class GetVideosByQuery(
    private val videoService: VideoService,
    private val eventService: EventService,
    private val userService: UserService,
    private val searchQueryConverter: SearchQueryConverter
) {
    companion object : KLogging()

    operator fun invoke(
        query: String,
        sortBy: SortKey?,
        bestFor: List<String>?,
        minDurationString: String?,
        maxDurationString: String?,
        duration: List<String>?,
        releasedDateFrom: String?,
        releasedDateTo: String?,
        pageSize: Int,
        pageNumber: Int,
        source: String?,
        ageRangeMin: Int?,
        ageRangeMax: Int?,
        ageRanges: List<AgeRange>,
        subjects: Set<String>,
        promoted: Boolean?,
        contentPartnerNames: Set<String>,
        type: Set<String>,
        user: User,
        subjectsSetManually: Boolean?,
        isClassroom: Boolean?
    ): Page<Video> {
        validatePageSize(pageSize)
        validatePageNumber(pageNumber)

        val userSubjectIds =
            user.let { userService.getSubjectIds(it.id.value) } ?: emptySet()

        val request = VideoRequest(
            text = query,
            sortBy = sortBy,
            pageIndex = pageNumber,
            pageSize = pageSize,
            bestFor = bestFor,
            durationRanges = searchQueryConverter.convertDurations(minDurationString, maxDurationString, duration),
            source = searchQueryConverter.convertSource(source),
            releaseDateFrom = searchQueryConverter.convertDate(releasedDateFrom),
            releaseDateTo = searchQueryConverter.convertDate(releasedDateTo),
            ageRangeMin = ageRangeMin,
            ageRangeMax = ageRangeMax,
            ageRanges = ageRanges,
            userSubjectIds = userSubjectIds,
            subjectsRequest = SubjectsRequest(ids = subjects, setManually = subjectsSetManually),
            promoted = promoted,
            contentPartnerNames = contentPartnerNames,
            type = type.map { searchQueryConverter.convertType(it) }.toSet(),
            isClassroom = isClassroom
        )

        val videoSearchResponse = videoService.search(request = request, videoAccess = user.accessRules.videoAccess)
        logger.info { "Found ${videoSearchResponse.counts.total} videos for query $request" }

        eventService.saveSearchEvent(
            query = query,
            pageIndex = pageNumber,
            pageSize = pageSize,
            totalResults = videoSearchResponse.counts.total,
            pageVideoIds = videoSearchResponse.videos.map { it.videoId.value },
            user = user
        )

        return Page(
            elements = videoSearchResponse.videos.asIterable(),
            pageInfo = PageInfo(
                hasMoreElements = (pageNumber + 1) * pageSize < videoSearchResponse.counts.total,
                totalElements = videoSearchResponse.counts.total,
                pageRequest = PageRequest(page = pageNumber, size = pageSize)
            )
        )
    }

    private fun validatePageNumber(pageNumber: Int) {
        if (pageNumber < 0) throw IllegalArgumentException()
    }

    private fun validatePageSize(pageSize: Int) {
        if (pageSize > MAX_PAGE_SIZE) throw IllegalArgumentException()
        if (pageSize <= 0) throw IllegalArgumentException()
    }
}

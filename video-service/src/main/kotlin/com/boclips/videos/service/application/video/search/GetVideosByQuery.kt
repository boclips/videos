package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.common.ResultsPage
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoCounts
import com.boclips.videos.service.domain.model.video.request.FixedAgeRangeFacet
import com.boclips.videos.service.domain.model.video.request.SortKey
import com.boclips.videos.service.domain.model.video.request.SubjectsRequest
import com.boclips.videos.service.domain.model.video.request.VideoRequest
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.domain.service.video.VideoRetrievalService
import com.boclips.videos.service.presentation.VideoController.Companion.MAX_PAGE_SIZE
import mu.KLogging

class GetVideosByQuery(
    private val videoRetrievalService: VideoRetrievalService,
    private val eventService: EventService,
    private val userService: UserService,
    private val searchQueryConverter: SearchQueryConverter
) {
    companion object : KLogging()

    operator fun invoke(
        query: String,
        ids: Set<String>,
        sortBy: SortKey?,
        bestFor: List<String>?,
        minDurationString: String?,
        maxDurationString: String?,
        duration: List<String>?,
        durationFacets: List<String>?,
        releasedDateTo: String?,
        pageSize: Int,
        pageNumber: Int,
        source: String?,
        ageRangeMin: Int?,
        ageRangeMax: Int?,
        ageRanges: List<AgeRange>,
        ageRangesFacets: List<FixedAgeRangeFacet>?,
        subjects: Set<String>,
        promoted: Boolean?,
        channelNames: Set<String>,
        type: Set<String>,
        user: User,
        subjectsSetManually: Boolean?,
        releasedDateFrom: String?,
        resourceTypes: Set<String>,
        resourceTypeFacets: List<String>?
        ): ResultsPage<Video, VideoCounts> {
        validatePageSize(pageSize)
        validatePageNumber(pageNumber)

        val userSubjectIds =
            user.let { userService.getSubjectIds(it.id.value) } ?: emptySet()

        val request = VideoRequest(
            ids = ids,
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
            subjectsRequest = SubjectsRequest(
                ids = subjects,
                setManually = subjectsSetManually
            ),
            promoted = promoted,
            channelNames = channelNames,
            type = type.map { searchQueryConverter.convertType(it) }.toSet(),
            facets = FacetConverter().invoke(ageRangesFacets, durationFacets, resourceTypeFacets),
            attachmentTypes = resourceTypes
        )

        val videoSearchResponse = videoRetrievalService.searchPlaybableVideos(request = request, videoAccess = user.accessRules.videoAccess)
        logger.info { "Found ${videoSearchResponse.counts.total} videos for query $request" }

        eventService.saveSearchEvent(
            query = query,
            pageIndex = pageNumber,
            pageSize = pageSize,
            totalResults = videoSearchResponse.counts.total,
            pageVideoIds = videoSearchResponse.videos.map { it.videoId.value },
            user = user
        )

        return ResultsPage(
            elements = videoSearchResponse.videos.asIterable(),
            counts = videoSearchResponse.counts,
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

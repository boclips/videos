package com.boclips.videos.service.application.video.search

import com.boclips.security.utils.Client
import com.boclips.security.utils.ClientExtractor
import com.boclips.videos.service.application.common.QueryConverter
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.common.ResultsPage
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.user.Organisation
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoCounts
import com.boclips.videos.service.domain.model.video.request.*
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.presentation.VideoController.Companion.MAX_PAGE_SIZE
import mu.KLogging
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.math.BigDecimal
import java.time.LocalDate
import javax.servlet.http.HttpServletRequest

class GetVideosByQuery(
    private val retrievePlayableVideos: RetrievePlayableVideos,
    private val eventService: EventService,
    private val userService: UserService,
    private val queryConverter: QueryConverter
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
        channelIds: Set<String>,
        type: Set<String>,
        user: User,
        userOrganisation: Organisation?,
        subjectsSetManually: Boolean?,
        releasedDateFrom: String?,
        resourceTypes: Set<String>,
        resourceTypeFacets: List<String>?,
        videoTypeFacets: List<String>?,
        includeChannelFacets: Boolean?,
        includePriceFacets: Boolean?,
        queryParams: Map<String, List<String>>,
        prices: Set<BigDecimal>,
        categoryCodes: Set<String>,
        updatedAtFrom: String?
    ): ResultsPage<Video, VideoCounts> {
        validatePageSize(pageSize)
        validatePageNumber(pageNumber)

        val userSubjectIds = when (ClientExtractor.extractClient()) {
            is Client.Teachers -> user.id?.let { userService.getSubjectIds(it.value) } ?: emptySet()
            else -> emptySet()
        }

        val request = VideoRequest(
            ids = ids,
            text = query,
            sortBy = sortBy,
            pagingState = VideoRequestPagingState.PageNumber(pageNumber),
            pageSize = pageSize,
            bestFor = bestFor,
            durationRanges = queryConverter.convertDurations(minDurationString, maxDurationString, duration),
            source = queryConverter.convertSource(source),
            releaseDateFrom = queryConverter.convertDate(releasedDateFrom),
            releaseDateTo = queryConverter.convertDate(releasedDateTo),
            ageRangeMin = ageRangeMin,
            ageRangeMax = ageRangeMax,
            ageRanges = ageRanges,
            userSubjectIds = userSubjectIds,
            subjectsRequest = SubjectsRequest(
                ids = subjects,
                setManually = subjectsSetManually
            ),
            promoted = promoted,
            channelIds = channelIds,
            types = type.map { queryConverter.convertTypeToVideoType(it) }.toSet(),
            facets = FacetConverter().invoke(
                ageRangesFacets,
                durationFacets,
                resourceTypeFacets,
                videoTypeFacets,
                includeChannelFacets,
                includePriceFacets
            ),
            attachmentTypes = resourceTypes,
            userOrganisationId = userOrganisation?.organisationId,
            prices = prices,
            categoryCodes = categoryCodes,
            updatedAtFrom = LocalDate.parse(updatedAtFrom)
        )

        val videoSearchResponse =
            retrievePlayableVideos.searchPlayableVideos(
                request = request,
                videoAccess = user.accessRules.videoAccess
            )
        logger.info { "Found ${videoSearchResponse.counts.total} videos for query $request" }

        try {
            eventService.saveSearchEvent(
                query = query,
                pageIndex = pageNumber,
                pageSize = pageSize,
                totalResults = videoSearchResponse.counts.total,
                pageVideoIds = videoSearchResponse.videos.map { it.videoId.value },
                queryParams = queryParams,
                user = user
            )
        } catch (e: Exception) {
            val errorMessage = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes?)
                ?.request
                ?.let { requestToString(it) }
                ?: "???"
            logger.error(e) { "Error logging search event for request $errorMessage" }
        }

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

    private fun requestToString(request: HttpServletRequest): String {
        val headers =
            request.headerNames.toList().map { headerName -> "$headerName ${request.getHeader(headerName)}" }
                .joinToString()
        return "${request.method} ${request.requestURI} [ headers: $headers ]"
    }

    private fun validatePageNumber(pageNumber: Int) {
        if (pageNumber < 0) throw IllegalArgumentException()
    }

    private fun validatePageSize(pageSize: Int) {
        if (pageSize > MAX_PAGE_SIZE) throw IllegalArgumentException()
        if (pageSize <= 0) throw IllegalArgumentException()
    }
}

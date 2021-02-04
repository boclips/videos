package com.boclips.videos.service.application.video.search

import com.boclips.videos.api.request.Projection
import com.boclips.videos.service.application.video.exceptions.SearchRequestValidationException
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.common.ResultsPage
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.user.Organisation
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.user.UserNotAssignedToOrganisationException
import com.boclips.videos.service.domain.model.video.*
import com.boclips.videos.service.domain.model.video.prices.PricedVideo
import com.boclips.videos.service.domain.model.video.request.SortKey
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.plackback.PlaybackUpdateService
import com.boclips.videos.service.presentation.converters.convertAgeRangeFacets
import com.boclips.videos.service.presentation.converters.convertAgeRanges
import com.boclips.web.exceptions.ResourceNotFoundApiException
import java.math.BigDecimal

class SearchVideo(
    private val getVideoById: GetVideoById,
    private val getVideosByQuery: GetVideosByQuery,
    private val videoRepository: VideoRepository,
    private val playbackUpdateService: PlaybackUpdateService,
    private val userService: UserService,
    private val priceComputingService: PriceComputingService
) {
    companion object {
        fun isAlias(potentialAlias: String): Boolean = Regex("\\d+").matches(potentialAlias)
    }

    fun byId(id: String?, user: User, projection: Projection? = null): BaseVideo {
        val videoId = resolveToAssetId(id)!!
        if (projection == Projection.full) {
            playbackUpdateService.updatePlaybacksFor(VideoFilter.HasVideoId(videoId))
        }
        val retrievedVideo = getVideoById(videoId, user)
        return addOrganisationPrice(retrievedVideo, user)
    }

    fun byQuery(
        query: String?,
        ids: Set<String> = emptySet(),
        sortBy: SortKey? = null,
        bestFor: List<String>? = null,
        minDuration: String? = null,
        maxDuration: String? = null,
        duration: List<String>? = null,
        durationFacets: List<String>? = null,
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
        channelNames: Set<String> = emptySet(),
        channelIds: Set<String> = emptySet(),
        type: Set<String> = emptySet(),
        user: User,
        resourceTypes: Set<String> = emptySet(),
        resourceTypeFacets: List<String>? = null,
        videoTypeFacets: List<String>? = null,
        includeChannelFacets: Boolean? = null,
        prices: Set<BigDecimal> = emptySet(),
        includePriceFacets: Boolean? = false,
        queryParams: Map<String, List<String>>? = null
    ): ResultsPage<out BaseVideo, VideoCounts> {
        val userOrganisation = userService.getOrganisationOfUser(user.idOrThrow().value)

        val retrievedVideos = getVideosByQuery(
            query = query ?: "",
            ids = ids.mapNotNull { resolveToAssetId(it, false)?.value }.toSet(),
            sortBy = sortBy,
            bestFor = bestFor,
            minDurationString = minDuration,
            maxDurationString = maxDuration,
            duration = duration,
            durationFacets = durationFacets,
            releasedDateFrom = releasedDateFrom,
            releasedDateTo = releasedDateTo,
            pageSize = pageSize,
            pageNumber = pageNumber,
            source = source,
            ageRangeMin = ageRangeMin,
            ageRangeMax = ageRangeMax,
            ageRanges = ageRanges?.map(::convertAgeRanges) ?: emptyList(),
            ageRangesFacets = ageRangeFacets?.map(::convertAgeRangeFacets),
            subjects = subjects,
            subjectsSetManually = subjectsSetManually,
            promoted = promoted,
            channelNames = channelNames,
            channelIds = channelIds,
            type = type,
            user = user,
            userOrganisation = userOrganisation,
            resourceTypes = resourceTypes.mapTo(HashSet()) { AttachmentType.valueOf(it).label },
            resourceTypeFacets = resourceTypeFacets,
            videoTypeFacets = videoTypeFacets,
            includeChannelFacets = includeChannelFacets,
            includePriceFacets = includePriceFacets,
            queryParams = queryParams ?: emptyMap(),
            prices = prices
        )
        return addOrganisationPrices(retrievedVideos, userOrganisation)
    }

    private fun addOrganisationPrices(
        retrievedVideos: ResultsPage<Video, VideoCounts>,
        userOrganisation: Organisation?
    ): ResultsPage<PricedVideo, VideoCounts> {
        val videoTypePrices = userOrganisation?.deal?.prices
        val pricedVideos = retrievedVideos.elements.map {
            PricedVideo(
                it,
                priceComputingService.computeVideoPrice(it, videoTypePrices)
            )
        }
        return ResultsPage(
            elements = pricedVideos,
            counts = retrievedVideos.counts,
            pageInfo = retrievedVideos.pageInfo
        )
    }

    private fun addOrganisationPrice(
        retrievedVideo: Video,
        user: User
    ): PricedVideo {
        val videoTypePrices = user.id?.let { userService.getOrganisationOfUser(it.value)?.deal?.prices }
        val videoPrice = priceComputingService.computeVideoPrice(retrievedVideo, videoTypePrices)
        return PricedVideo(retrievedVideo, videoPrice)
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

package com.boclips.videos.service.application.video.search

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.search.service.domain.common.FacetType
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.users.api.httpclient.OrganisationsClient
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.*
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.domain.model.video.request.VideoRequest
import com.boclips.videos.service.domain.model.video.request.VideoRequestPagingState
import com.boclips.videos.service.domain.service.video.VideoIndex
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.infrastructure.convertPageToIndex
import mu.KLogging

class RetrievePlayableVideos(
        private val videoRepository: VideoRepository,
        private val videoIndex: VideoIndex
) {
    companion object : KLogging()

    fun searchPlayableVideos(request: VideoRequest, videoAccess: VideoAccess): VideoResults {
        val pageIndex = when (request.pagingState) {
            is VideoRequestPagingState.PageNumber -> request.pagingState.number
            is VideoRequestPagingState.Cursor -> 0
        }

        logger.info { "Searching for videos with access $videoAccess" }

        val searchRequest = PaginatedIndexSearchRequest(
            query = request.toQuery(videoAccess),
            startIndex = convertPageToIndex(request.pageSize, pageIndex),
            windowSize = request.pageSize
        )

        val results = videoIndex.search(searchRequest)

        val videoIds = results.elements.map { VideoId(value = it) }
        val playableVideos = videoRepository.findAll(videoIds = videoIds).filter { it.isPlayable() }

        val subjectCounts = results.counts.getFacetCounts(FacetType.Subjects)
            .map { SubjectFacet(subjectId = SubjectId(it.id), total = it.hits) }
        val ageRangeCounts = results.counts.getFacetCounts(FacetType.AgeRanges)
            .map { AgeRangeFacet(ageRangeId = AgeRangeId(it.id), total = it.hits) }
        val durationCounts = results.counts.getFacetCounts(FacetType.Duration)
            .map { DurationFacet(durationId = it.id, total = it.hits) }
        val attachmentTypeCounts = results.counts.getFacetCounts(FacetType.AttachmentTypes)
            .map { AttachmentTypeFacet(attachmentType = it.id, total = it.hits) }
        val channelCounts = results.counts.getFacetCounts(FacetType.Channels)
            .map { ChannelFacet(channelId = ChannelId(it.id), total = it.hits) }
        val videoTypeCounts = results.counts.getFacetCounts(FacetType.VideoTypes)
            .map { VideoTypeFacet(typeId = it.id, total = it.hits) }
        val priceCounts = results.counts.getFacetCounts(FacetType.Prices).map {
            PriceFacet(price = it.id, total = it.hits)
        }

        logger.info { "Retrieving ${playableVideos.size} videos for query $request" }

        return VideoResults(
            videos = playableVideos,
            counts = VideoCounts(
                total = results.counts.totalHits,
                subjects = subjectCounts,
                ageRanges = ageRangeCounts,
                durations = durationCounts,
                attachmentTypes = attachmentTypeCounts,
                channels = channelCounts,
                videoTypes = videoTypeCounts,
                prices = priceCounts
            )
        )
    }
}

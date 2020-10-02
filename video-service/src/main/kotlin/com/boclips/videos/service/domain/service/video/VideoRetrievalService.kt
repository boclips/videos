package com.boclips.videos.service.domain.service.video

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.search.service.domain.common.FacetType
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.AgeRangeFacet
import com.boclips.videos.service.domain.model.video.AttachmentTypeFacet
import com.boclips.videos.service.domain.model.video.ChannelFacet
import com.boclips.videos.service.domain.model.video.DurationFacet
import com.boclips.videos.service.domain.model.video.SubjectFacet
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VideoCounts
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoResults
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.domain.model.video.request.VideoIdsRequest
import com.boclips.videos.service.domain.model.video.request.VideoRequest
import com.boclips.videos.service.infrastructure.convertPageToIndex
import mu.KLogging

class VideoRetrievalService(
    private val videoRepository: VideoRepository,
    private val videoIndex: VideoIndex
) {
    companion object : KLogging()

    private val defaultAccessRules = listOf(
        VideoAccessRule.IncludedDistributionMethods(
            setOf(
                DistributionMethod.STREAM
            )
        )
    )

    fun searchPlayableVideos(request: VideoRequest, videoAccess: VideoAccess): VideoResults {
        /**
         * Default access rules aren't technically needed anymore as they have been added to the classroom content package.
         * Though, as it's a legal restriction (can't get non-streamable videos) we might want to enforce it here as well.
         * I guess it depends if we see this search method serving up just streamable videos.
         */
        val videoAccessWithDefaultRules = withDefaultRules(videoAccess)

        logger.info { "Searching for videos with access $videoAccess" }

        val searchRequest = PaginatedSearchRequest(
            query = request.toQuery(videoAccessWithDefaultRules),
            startIndex = convertPageToIndex(request.pageSize, request.pageIndex),
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

        logger.info { "Retrieving ${playableVideos.size} videos for query $request" }

        return VideoResults(
            videos = playableVideos,
            counts = VideoCounts(
                total = results.counts.totalHits,
                subjects = subjectCounts,
                ageRanges = ageRangeCounts,
                durations = durationCounts,
                attachmentTypes = attachmentTypeCounts,
                channels = channelCounts
            )
        )
    }

    fun getPlayableVideos(videoIds: List<VideoId>, videoAccess: VideoAccess): List<Video> {
        val orderById = videoIds.withIndex().associate { it.value to it.index }

        val results = videoIndex.search(
            PaginatedSearchRequest(
                VideoIdsRequest(ids = videoIds).toSearchQuery(videoAccess),
                windowSize = videoIds.size
            )
        )

        return results.elements.map { VideoId(value = it) }
            .let { videoRepository.findAll(it) }
            .also { videos ->
                if (videoIds.size != videos.size) {
                    logger.info {
                        val videosNotFound = videoIds - videos.map { it.videoId }
                        "Some of the requested video videos could not be found. Ids not found: $videosNotFound"
                    }
                }
            }
            .filter { it.isPlayable() }
            .sortedBy { orderById[it.videoId] }
    }

    fun getPlayableVideo(videoId: VideoId, videoAccess: VideoAccess): Video {
        logger.info { "Getting playable video: $videoId, with access: $videoAccess" }

        val results = videoIndex.search(
            PaginatedSearchRequest(
                query = VideoIdsRequest(ids = listOf(videoId)).toSearchQuery(videoAccess),
                windowSize = 1
            )
        )

        return results.elements
            .firstOrNull()
            ?.let {
                val video = videoRepository.find(videoId) ?: throw VideoNotFoundException(videoId)
                if (!video.isPlayable()) throw VideoPlaybackNotFound()
                logger.info { "Retrieved playable video $videoId" }
                video
            }
            ?: throw VideoNotFoundException().also {
                logger.info { "Could not find playable video $videoId with access rules $videoAccess" }
            }
    }

    private fun withDefaultRules(videoAccess: VideoAccess): VideoAccess.Rules {
        return when (videoAccess) {
            VideoAccess.Everything -> VideoAccess.Rules(defaultAccessRules)
            is VideoAccess.Rules -> VideoAccess.Rules(videoAccess.accessRules + defaultAccessRules)
        }
    }
}

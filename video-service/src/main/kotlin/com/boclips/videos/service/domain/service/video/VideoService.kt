package com.boclips.videos.service.domain.service.video

import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.boclips.search.service.domain.common.Bucket
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.UnboundedAgeRange
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.SubjectCount
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VideoCounts
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoIdsQuery
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.model.video.VideoSearchQuery
import com.boclips.videos.service.infrastructure.convertPageToIndex
import mu.KLogging

class VideoService(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val videoRepository: VideoRepository,
    private val videoSearchService: VideoSearchService
) {
    companion object : KLogging()

    private val defaultAccessRules = listOf(
        VideoAccessRule.IncludedDistributionMethods(
            setOf(
                DistributionMethod.STREAM
            )
        )
    )

    fun search(query: VideoSearchQuery, videoAccess: VideoAccess): List<Video> {
        val videoAccessWithDefaultRules = withDefaultRules(videoAccess)

        val searchRequest = PaginatedSearchRequest(
            query = query.toSearchQuery(videoAccessWithDefaultRules),
            startIndex = convertPageToIndex(query.pageSize, query.pageIndex),
            windowSize = query.pageSize
        )
        val videoIds = videoSearchService.search(searchRequest).map { VideoId(value = it) }
        val playableVideos = videoRepository.findAll(videoIds = videoIds).filter { it.isPlayable() }

        logger.info { "Retrieving ${playableVideos.size} videos for query $query" }

        return playableVideos
    }

    fun count(videoSearchQuery: VideoSearchQuery, videoAccess: VideoAccess): VideoCounts {
        logger.info { "Counting videos for query $videoSearchQuery" }

        val videoAccessWithDefaultRules = withDefaultRules(videoAccess)
        val counts = videoSearchService.count(videoSearchQuery.toSearchQuery(videoAccess = videoAccessWithDefaultRules))

        val subjectCounts =
            counts.getCounts(Bucket.SubjectsBucket).map { SubjectCount(subjectId = SubjectId(it.id), total = it.hits) }

        return VideoCounts(total = counts.hits, subjects = subjectCounts)
    }

    fun getPlayableVideos(videoIds: List<VideoId>, videoAccess: VideoAccess): List<Video> {
        val orderById = videoIds.withIndex().associate { it.value to it.index }

        return videoSearchService.search(
                PaginatedSearchRequest(
                    VideoIdsQuery(ids = videoIds).toSearchQuery(
                        videoAccess
                    ),
                    windowSize = videoIds.size
                )
            ).map { VideoId(value = it) }
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

    fun create(videoToBeCreated: Video): Video {
        if (videoRepository.existsVideoFromContentPartnerId(
                videoToBeCreated.contentPartner.contentPartnerId,
                videoToBeCreated.videoReference
            )
        ) {
            logger.info { "Detected duplicate for $videoToBeCreated." }
            throw VideoNotCreatedException(videoToBeCreated)
        }

        var ageRange = videoToBeCreated.ageRange
        if (videoToBeCreated.ageRange is UnboundedAgeRange) {
            contentPartnerRepository.findById(
                    contentPartnerId = ContentPartnerId(
                        value = videoToBeCreated.contentPartner.contentPartnerId.value
                    )
                )
                ?.apply {
                    ageRange = if (this.ageRangeBuckets.min != null && this.ageRangeBuckets.max != null) {
                        AgeRange.bounded(this.ageRangeBuckets.min, this.ageRangeBuckets.max)
                    } else {
                        AgeRange.unbounded()
                    }
                }
        }

        return videoRepository.create(videoToBeCreated.copy(ageRange = ageRange))
    }

    fun getPlayableVideo(videoId: VideoId, videoAccess: VideoAccess): Video {
        return videoSearchService.search(
                PaginatedSearchRequest(
                    query = VideoIdsQuery(ids = listOf(videoId)).toSearchQuery(
                        videoAccess
                    ),
                    windowSize = 1
                )
            )
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


package com.boclips.videos.service.domain.service.video

import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.UnboundedAgeRange
import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoId
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

    fun search(query: VideoSearchQuery, videoAccessRule: VideoAccessRule): List<Video> {
        val searchRequest = PaginatedSearchRequest(
            query = query.toSearchQuery(videoAccessRule),
            startIndex = convertPageToIndex(query.pageSize, query.pageIndex),
            windowSize = query.pageSize
        )
        val videoIds = videoSearchService.search(searchRequest).map { VideoId(value = it) }
        val playableVideos = videoRepository.findAll(videoIds = videoIds).filter { it.isPlayable() }

        logger.info { "Returning ${playableVideos.size} videos for query $query" }

        return playableVideos
    }

    fun count(videoSearchQuery: VideoSearchQuery, videoAccessRule: VideoAccessRule): Long {
        logger.info { "Counted videos for query $videoSearchQuery" }
        return videoSearchService.count(videoSearchQuery.toSearchQuery(videoAccessRule))
    }

    fun getPlayableVideo(
        videoId: VideoId,
        videoAccessRule: VideoAccessRule
    ): Video =
        when (videoAccessRule) {
            is VideoAccessRule.SpecificIds -> {
                if (!videoAccessRule.videoIds.contains(videoId)) {
                    throw VideoNotFoundException()
                }

                getPlayableVideo(videoId)
            }
            VideoAccessRule.Everything -> getPlayableVideo(videoId)
        }

    fun getPlayableVideos(videoIds: List<VideoId>, accessRule: VideoAccessRule): List<Video> {
        val permittedVideoIds = when (accessRule) {
            is VideoAccessRule.SpecificIds -> videoIds.intersect(accessRule.videoIds)
            VideoAccessRule.Everything -> videoIds
        }.toList()

        val videos = videoRepository.findAll(permittedVideoIds)

        if (videoIds.size != videos.size) {
            logger.info {
                val videosNotFound = videoIds - videos.map { it.videoId }
                "Some of the requested video videos could not be found. Ids not found: $videosNotFound"
            }
        }

        return videos.filter { it.isPlayable() }
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

        var newAgeRange = videoToBeCreated.ageRange
        if (videoToBeCreated.ageRange is UnboundedAgeRange) {
            contentPartnerRepository.findById(videoToBeCreated.contentPartner.contentPartnerId)
                ?.apply {
                    newAgeRange = if (this.ageRange.min() != null && this.ageRange.max() != null) {
                        AgeRange.bounded(this.ageRange.min(), this.ageRange.max())
                    } else {
                        AgeRange.unbounded()
                    }
                }
        }

        return videoRepository.create(videoToBeCreated.copy(ageRange = newAgeRange))
    }

    //TODO Event Interface
    fun updateContentPartnerInVideos(contentPartner: com.boclips.contentpartner.service.domain.model.ContentPartner) {
        logger.info { "Starting updating videos for content partner: $contentPartner" }

        videoRepository.streamUpdate(
            VideoFilter.ContentPartnerIdIs(contentPartnerId = contentPartner.contentPartnerId)
        ) { videos ->
            videos.flatMap { video ->
                listOfNotNull(
                    VideoUpdateCommand.ReplaceContentPartner(
                        videoId = video.videoId,
                        contentPartner = ContentPartner(
                            contentPartnerId = contentPartner.contentPartnerId,
                            name = contentPartner.name
                        )
                    ),
                    VideoUpdateCommand.ReplaceAgeRange(
                        videoId = video.videoId,
                        ageRange = AgeRange.bounded(contentPartner.ageRange.min(), contentPartner.ageRange.max())
                    ),
                    VideoUpdateCommand.ReplaceDistributionMethods(
                        videoId = video.videoId,
                        distributionMethods = contentPartner.distributionMethods
                    ),
                    contentPartner.legalRestrictions?.let {
                        VideoUpdateCommand.ReplaceLegalRestrictions(
                            videoId = video.videoId,
                            legalRestrictions = contentPartner.legalRestrictions
                        )
                    }
                )
            }
        }

        logger.info { "Finished updating videos for content partner: $contentPartner" }
    }

    private fun getPlayableVideo(videoId: VideoId): Video {
        val video = videoRepository.find(videoId) ?: throw VideoNotFoundException(videoId)
        if (!video.isPlayable()) throw VideoPlaybackNotFound()

        logger.info { "Retrieved playable video $videoId" }
        return video
    }
}


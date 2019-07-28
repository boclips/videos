package com.boclips.videos.service.domain.service.video

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForDownload
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForStream
import com.boclips.videos.service.domain.model.common.UnboundedAgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.model.video.VideoSearchQuery
import com.boclips.videos.service.infrastructure.convertPageToIndex
import mu.KLogging

class VideoService(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val videoRepository: VideoRepository,
    private val videoSearchService: VideoSearchService,
    private val includeVideosInSearchForStream: IncludeVideosInSearchForStream,
    private val includeVideosInSearchForDownload: IncludeVideosInSearchForDownload
) {
    companion object : KLogging()

    fun search(query: VideoSearchQuery): List<Video> {
        val searchRequest = PaginatedSearchRequest(
            query = query.toSearchQuery(),
            startIndex = convertPageToIndex(query.pageSize, query.pageIndex),
            windowSize = query.pageSize
        )
        val videoIds = videoSearchService.search(searchRequest).map { VideoId(value = it) }
        val playableVideos = videoRepository.findAll(videoIds = videoIds).filter { it.isPlayable() }

        logger.info { "Returning ${playableVideos.size} videos for query $query" }

        return playableVideos
    }

    // TODO this returns all videos matched by query, does not take into account whether video is playable
    fun count(videoSearchQuery: VideoSearchQuery): Long {
        logger.info { "Counted videos for query $videoSearchQuery" }
        return videoSearchService.count(videoSearchQuery.toSearchQuery())
    }

    fun getPlayableVideo(videoId: VideoId): Video {
        val video = videoRepository.find(videoId) ?: throw VideoNotFoundException(videoId)
        if (!video.isPlayable()) throw VideoPlaybackNotFound()

        logger.info { "Retrieved playable video $videoId" }
        return video
    }

    fun getPlayableVideo(videoIds: List<VideoId>): List<Video> {
        val videos = videoRepository.findAll(videoIds)

        if (videoIds.size != videos.size) {
            logger.info {
                val videosNotFound = videoIds - videos.map { it.videoId }
                "Some of the requested video videos could not be found. Ids not found: $videosNotFound"
            }
        }

        return videos.filter { it.isPlayable() }
    }

    fun create(videoToBeCreated: Video): Video {
        var newAgeRange = videoToBeCreated.ageRange

        if (videoToBeCreated.ageRange is UnboundedAgeRange) {
            contentPartnerRepository.findByName(videoToBeCreated.contentPartner.name)
                ?.apply { newAgeRange = this.ageRange }
        }

        val createdVideo = videoRepository.create(videoToBeCreated.copy(ageRange = newAgeRange))

        if (videoToBeCreated.contentPartner.isStreamable()) {
            includeVideosInSearchForStream(videoIds = listOf(createdVideo.videoId.value))
        }

        if (videoToBeCreated.contentPartner.isDownloadable()) {
            if (createdVideo.isBoclipsHosted()) {
                includeVideosInSearchForDownload(videoIds = listOf(createdVideo.videoId.value))
            }
        }

        return createdVideo
    }

    fun getPlayableVideos(contentPartnerId: ContentPartnerId): List<Video> {
        return videoRepository.findByContentPartnerId(contentPartnerId)
    }
}


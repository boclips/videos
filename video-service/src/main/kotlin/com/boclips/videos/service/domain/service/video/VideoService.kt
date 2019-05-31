package com.boclips.videos.service.domain.service.video

import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.domain.model.ageRange.UnboundedAgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.infrastructure.convertPageToIndex
import mu.KLogging

class VideoService(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val videoRepository: VideoRepository,
    private val searchService: SearchService
) {
    companion object : KLogging()

    fun search(query: VideoSearchQuery): List<Video> {
        val searchRequest = PaginatedSearchRequest(
            query = query.toSearchQuery(),
            startIndex = convertPageToIndex(query.pageSize, query.pageIndex),
            windowSize = query.pageSize
        )
        val videoIds = searchService.search(searchRequest).map { VideoId(value = it) }
        val playableVideos = videoRepository.findAll(videoIds = videoIds).filter { it.isPlayable() }

        logger.info { "Returning ${playableVideos.size} videos for query $query" }

        return playableVideos
    }

    // TODO this returns all videos matched by query, does not take into account whether video is playable
    fun count(videoSearchQuery: VideoSearchQuery): Long {
        logger.info { "Counted videos for query $videoSearchQuery" }
        return searchService.count(videoSearchQuery.toSearchQuery())
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
                "Some of the requested video videos could not be found. Ids found: $videosNotFound"
            }
        }

        return videos.filter { it.isPlayable() }
    }

    fun create(videoToBeCreated: Video): Video {
        var ageRange = videoToBeCreated.ageRange

        if(videoToBeCreated.ageRange is UnboundedAgeRange) {
            contentPartnerRepository.findByName(videoToBeCreated.contentPartnerName)
                ?.apply { ageRange = this.ageRange }

        }
        return videoRepository.create(videoToBeCreated.copy(ageRange = ageRange))
    }

    fun setDefaultAgeRange(contentPartner: ContentPartner) : List<Video> {
        return getVideosByContentPartner(contentPartner.name).map { setDefaultAgeRange(it.videoId, contentPartner.ageRange) }
    }

    private fun getVideosByContentPartner(contentPartnerName: String): List<Video> {
        return videoRepository.findByContentPartner(contentPartnerName)
    }

    private fun setDefaultAgeRange(videoId: VideoId, ageRange: AgeRange): Video {
        return videoRepository.update(VideoUpdateCommand.ReplaceAgeRange(videoId = videoId, ageRange = ageRange))
    }
}


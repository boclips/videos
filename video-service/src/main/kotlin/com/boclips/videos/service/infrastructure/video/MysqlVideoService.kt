package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.application.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.domain.service.VideoService
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class MysqlVideoService(
        private val searchService: SearchService,
        private val videoRepository: VideoRepository
) : VideoService {
    override fun findVideosBy(query: VideoSearchQuery): List<Video> {
        val videoIds = searchService.search(query.text)
        return findVideosBy(videoIds)
    }

    override fun findVideosBy(videoIds: List<VideoId>): List<Video> {
        val allFoundVideos = videoRepository.findAllById(videoIds.map { videoId -> videoId.videoId.toLong() })
        return allFoundVideos.map { videoEntity -> convertToVideo(videoEntity) }
    }

    override fun findVideoBy(videoId: VideoId): Video {
        val videoOptional = videoRepository.findById(videoId.videoId.toLong())
        val videoEntity = videoOptional.orElseThrow { VideoNotFoundException() }

        return convertToVideo(videoEntity)
    }

    private fun convertToVideo(videoEntity: VideoEntity): Video {
        return Video(
                videoId = VideoId(videoId = videoEntity.id.toString(), referenceId = videoEntity.reference_id),
                title = videoEntity.title!!,
                duration = Duration.between(
                        LocalTime.MIN,
                        LocalTime.parse(videoEntity.duration!!)
                ),
                description = videoEntity.description!!,
                releasedOn = LocalDate.parse(videoEntity.date!!),
                contentProvider = videoEntity.source!!,
                videoPlayback = null
        )
    }
}

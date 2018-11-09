package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoId

interface VideoRepository {

    fun findVideoBy(videoId: VideoId): Video?

    fun findVideosBy(videoIds: List<VideoId>): List<Video>

    fun findAllVideos(consumer: (videos: Sequence<Video>) -> Unit)

    fun deleteVideoById(videoId: VideoId)
}
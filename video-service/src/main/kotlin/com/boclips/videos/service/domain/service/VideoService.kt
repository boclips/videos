package com.boclips.videos.service.domain.service

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.model.VideoSearchQuery

interface VideoService {
    fun findAllVideos(consumer: (videos: Sequence<Video>) -> Unit)
    fun findVideosBy(query: VideoSearchQuery): List<Video>
    fun findVideosBy(videoIds: List<VideoId>): List<Video>
    @Throws(VideoNotFoundException::class)
    fun findVideoBy(videoId: VideoId): Video

    fun removeVideo(video: Video)
}
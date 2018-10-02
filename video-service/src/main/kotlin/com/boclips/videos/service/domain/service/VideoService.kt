package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.model.VideoSearchQuery

interface VideoService {
    fun findVideosBy(query: VideoSearchQuery): List<Video>
    fun findVideosBy(videoIds: List<VideoId>): List<Video>
    fun findVideoBy(videoId: VideoId): Video
    fun removeVideo(video: Video)
}


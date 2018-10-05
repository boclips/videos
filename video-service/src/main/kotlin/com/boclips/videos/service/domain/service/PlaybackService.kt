package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.Video

interface PlaybackService {
    fun getVideosWithPlayback(videos: List<Video>): List<Video>
    fun getVideoWithPlayback(video: Video): Video
    fun removePlayback(video: Video)
}
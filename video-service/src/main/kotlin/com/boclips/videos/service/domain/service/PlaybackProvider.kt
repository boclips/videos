package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.Video

interface PlaybackProvider {
    fun getVideosWithPlayback(videos: List<Video>): List<Video>
    fun removePlayback(video: Video)
}
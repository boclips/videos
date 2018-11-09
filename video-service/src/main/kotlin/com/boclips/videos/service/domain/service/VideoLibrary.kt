package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.VideoDetails
import com.boclips.videos.service.domain.model.VideoId

interface VideoLibrary {

    fun findVideoBy(videoId: VideoId): VideoDetails?

    fun findVideosBy(videoIds: List<VideoId>): List<VideoDetails>

    fun findAllVideos(consumer: (videos: Sequence<VideoDetails>) -> Unit)

    fun deleteVideoBy(videoId: VideoId)
}
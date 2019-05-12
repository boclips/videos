package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand

interface VideoRepository {
    fun find(videoId: VideoId): Video?
    fun findAll(videoIds: List<VideoId>): List<Video>
    fun streamAll(consumer: (Sequence<Video>) -> Unit)
    fun streamAll(filter: VideoFilter, consumer: (Sequence<Video>) -> Unit)
    fun delete(videoId: VideoId)
    fun create(video: Video): Video
    fun update(command: VideoUpdateCommand): Video
    fun bulkUpdate(commands: List<VideoUpdateCommand>)
    fun existsVideoFromContentPartner(contentPartnerId: String, partnerVideoId: String): Boolean
    fun resolveAlias(alias: String): VideoId?
}


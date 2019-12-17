package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.service.video.VideoUpdateCommand

interface VideoRepository {
    fun find(videoId: VideoId): Video?
    fun findAll(videoIds: List<VideoId>): List<Video>
    fun findByContentPartnerName(contentPartnerName: String): List<Video>
    fun findByContentPartnerId(contentPartnerId: ContentPartnerId): List<Video>
    fun streamAll(consumer: (Sequence<Video>) -> Unit)
    fun streamAll(filter: VideoFilter, consumer: (Sequence<Video>) -> Unit)
    fun create(video: Video): Video
    fun update(command: VideoUpdateCommand): Video
    fun delete(videoId: VideoId)
    fun bulkUpdate(commands: List<VideoUpdateCommand>): List<Video>
    fun existsVideoFromContentPartnerName(contentPartnerName: String, partnerVideoId: String): Boolean
    fun existsVideoFromContentPartnerId(contentPartnerId: ContentPartnerId, partnerVideoId: String): Boolean
    fun resolveAlias(alias: String): VideoId?
    fun streamUpdate(consumer: (List<Video>) -> List<VideoUpdateCommand>)
    fun streamUpdate(filter: VideoFilter, consumer: (List<Video>) -> List<VideoUpdateCommand>)
}


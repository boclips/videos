package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.contentpartner.ContentPartnerId

interface VideoRepository {
    fun find(videoId: VideoId): Video?
    fun findAll(videoIds: List<VideoId>): List<Video>
    fun findByContentPartnerName(contentPartnerName: String): List<Video>
    fun findByContentPartnerId(contentPartnerId: ContentPartnerId): List<Video>
    fun streamAll(consumer: (Sequence<Video>) -> Unit)
    fun streamAll(filter: VideoFilter, consumer: (Sequence<Video>) -> Unit)
    fun create(video: Video): Video
    fun update(command: VideoUpdateCommand): Video
    fun bulkUpdate(commands: List<VideoUpdateCommand>): List<Video>
    fun streamUpdate(consumer: (List<Video>) -> List<VideoUpdateCommand>)
    fun streamUpdate(filter: VideoFilter, consumer: (List<Video>) -> List<VideoUpdateCommand>)
    fun delete(videoId: VideoId)
    fun existsVideoFromContentPartnerName(contentPartnerName: String, partnerVideoId: String): Boolean
    fun findVideoByTitleFromContentPartnerName(contentPartnerName: String, videoTitle: String): Video?
    fun existsVideoFromContentPartnerId(contentPartnerId: ContentPartnerId, partnerVideoId: String): Boolean
    fun resolveAlias(alias: String): VideoId?
}


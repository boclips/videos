package com.boclips.videos.service.testsupport.fakes

import com.boclips.videos.service.domain.model.video.*
import com.boclips.videos.service.domain.model.video.channel.*
import com.boclips.videos.service.domain.service.video.*
import com.boclips.videos.service.infrastructure.video.converters.*
import com.boclips.videos.service.testsupport.*
import org.bson.types.*

class FakeVideoRepository : VideoRepository {

    private var nonExistentIds: List<String> = emptyList()

    override fun find(videoId: VideoId): Video? {
        TODO("Not yet implemented")
    }

    override fun findAll(videoIds: List<VideoId>): List<Video> {
        val videoDocuments = videoIds.mapNotNull {
            if (nonExistentIds.contains(it.value)) {
                null
            } else {
                VideoFactory.createVideoDocument(id = ObjectId(it.value))
            }
        }

        return videoDocuments.map(VideoDocumentConverter::toVideo)
    }

    override fun findByChannelName(channelName: String): List<Video> {
        TODO("Not yet implemented")
    }

    override fun findByChannelId(channelId: ChannelId): List<Video> {
        TODO("Not yet implemented")
    }

    override fun streamAll(consumer: (Sequence<Video>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun streamAll(filter: VideoFilter, consumer: (Sequence<Video>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun create(video: Video): Video {
        TODO("Not yet implemented")
    }

    override fun update(command: VideoUpdateCommand): Video {
        TODO("Not yet implemented")
    }

    override fun bulkUpdate(commands: List<VideoUpdateCommand>): List<Video> {
        TODO("Not yet implemented")
    }

    override fun streamUpdate(
        filter: VideoFilter,
        consumer: (List<Video>) -> List<VideoUpdateCommand>
    ): Sequence<Video> {
        TODO("Not yet implemented")
    }

    override fun delete(videoId: VideoId) {
        TODO("Not yet implemented")
    }

    override fun existsVideoFromChannelName(channelName: String, partnerVideoId: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun findVideoByTitleFromChannelName(channelName: String, videoTitle: String): Video? {
        TODO("Not yet implemented")
    }

    override fun existsVideoFromChannelId(channelId: ChannelId, partnerVideoId: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun resolveAlias(alias: String): VideoId? {
        TODO("Not yet implemented")
    }

    fun doesntExist(ids: List<String>) {
        nonExistentIds = ids
    }
}

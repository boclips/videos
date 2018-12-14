package com.boclips.videos.service.client.internal

import com.boclips.videos.service.client.CreateVideoRequest
import com.boclips.videos.service.client.VideoId
import com.boclips.videos.service.client.VideoServiceClient
import com.boclips.videos.service.client.spring.Video
import java.net.URI
import java.util.*

class FakeClient : VideoServiceClient {
    override fun get(id: VideoId): Video {
       return videos[id]!!
    }

    private val videos: MutableMap<VideoId, Video> = mutableMapOf()

    override fun create(request: CreateVideoRequest): VideoId {
        val videoId = VideoId(uri = URI("https://blah.com/videos/${UUID.randomUUID()}"))
        videos[videoId] = Video(
                videoId = videoId,
                subjects = request.subjects,
                contentPartnerId = request.provider,
                contentPartnerVideoId = request.providerVideoId
        )

        return videoId
    }

    override fun existsByContentPartnerInfo(contentPartnerId: String, contentPartnerVideoId: String): Boolean {
        return videos.values.any { it.contentPartnerId == contentPartnerId && it.contentPartnerVideoId == contentPartnerVideoId }
    }

    fun clear() {
        videos.clear()
    }

    fun getAllVideoRequests() = videos

    override fun setSubjects(id: VideoId, subjects: Set<String>) {
        val videoResource = videos[id]!!

        val newVideoResource = videoResource.copy(subjects = subjects)
        videos[id] = newVideoResource
    }
}

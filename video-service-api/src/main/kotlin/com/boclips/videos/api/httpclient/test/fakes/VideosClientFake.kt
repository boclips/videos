package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.request.video.AdminSearchRequest
import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.api.response.video.VideoResource
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources

class VideosClientFake : VideosClient {
    override fun getVideo(videoId: String): Resource<VideoResource> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getVideoTranscript(videoId: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createVideo(createVideoRequest: CreateVideoRequest): Resource<VideoResource> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteVideo(videoId: String): Resource<VideoResource> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateVideo(videoId: String): Resource<Void> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchVideos(searchRequest: AdminSearchRequest): Resources<VideoResource> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateVideoRating(videoId: String, rating: String): Resource<Void> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateVideoSharing(videoId: String): Resource<Void> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
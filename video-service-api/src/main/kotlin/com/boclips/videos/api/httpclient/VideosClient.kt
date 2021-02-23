package com.boclips.videos.api.httpclient

import com.boclips.videos.api.httpclient.helper.ObjectMapperDefinition
import com.boclips.videos.api.httpclient.helper.TokenFactory
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.api.request.video.SearchVideosRequest
import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.api.response.video.PriceResource
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideosResource
import com.fasterxml.jackson.databind.ObjectMapper
import feign.Client

import feign.Param
import feign.QueryMap
import feign.RequestLine

interface VideosClient {
    @RequestLine("GET /v1/videos/{videoId}?projection={projection}&userId={userId}")
    fun getVideo(
        @Param("videoId") videoId: String,
        @Param("projection") projection: Projection = Projection.details,
        @Param("userId") userId: String? = null
    ): VideoResource

    @RequestLine("GET /v1/videos/{videoId}/price?userId={userId}")
    fun getVideoPrice(
        @Param("videoId") videoId: String,
        @Param("userId") userId: String
    ): PriceResource

    @RequestLine("HEAD /v1/channels/{channelId}/videos/{channelVideoId}")
    fun probeVideoReference(
        @Param("channelId") channelId: String,
        @Param("channelVideoId") channelVideoId: String
    )

    @RequestLine("GET /v1/videos")
    fun searchVideos(@QueryMap searchVideosRequest: SearchVideosRequest = SearchVideosRequest()): VideosResource

    @RequestLine("POST /v1/videos")
    fun createVideo(createVideoRequest: CreateVideoRequest): VideoResource

    @RequestLine("PATCH /v1/videos/{videoId}")
    fun updateVideo(@Param("videoId") videoId: String, updateVideoRequest: UpdateVideoRequest = UpdateVideoRequest())

    @RequestLine("DELETE /v1/videos/{videoId}")
    fun deleteVideo(@Param("videoId") videoId: String)

    @RequestLine("PATCH /v1/videos/{videoId}?rating={rating}")
    fun updateVideoRating(@Param("videoId") videoId: String, @Param("rating") rating: Int)

    @RequestLine("PATCH /v1/videos/{videoId}?sharing=true")
    fun updateVideoSharing(@Param("videoId") videoId: String, @Param("sharing") sharing: Boolean)

    @RequestLine("GET /v1/videos/{videoId}/transcript")
    fun getVideoTranscript(@Param("videoId") videoId: String): String

    @RequestLine("PUT /v1/videos/{videoId}/captions?generated=true")
    fun requestVideoCaptions(@Param("videoId") videoId: String)

    companion object {
        @JvmStatic
        fun create(
            apiUrl: String,
            objectMapper: ObjectMapper = ObjectMapperDefinition.default(),
            tokenFactory: TokenFactory? = null,
            feignClient: Client
        ) = FeignInterserviceClientFactory.create(
                apiUrl,
                objectMapper,
                tokenFactory,
                feignClient,
                VideosClient::class.java
        )
    }
}

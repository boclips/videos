package com.boclips.videos.api.httpclient

import com.boclips.videos.api.httpclient.helper.ObjectMapperDefinition
import com.boclips.videos.api.httpclient.helper.TokenFactory
import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.api.request.video.SearchVideosRequest
import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideosResource
import com.fasterxml.jackson.databind.ObjectMapper
import feign.Feign
import feign.Logger
import feign.Param
import feign.QueryMap
import feign.RequestLine
import feign.RequestTemplate
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger

interface VideosClient {
    @RequestLine("GET /v1/videos/{videoId}")
    fun getVideo(
        @Param("videoId") videoId: String
    ): VideoResource

    @RequestLine("GET /v1/videos")
    fun searchVideos(
        @QueryMap searchVideosRequest: SearchVideosRequest = SearchVideosRequest()
    ): VideosResource

    @RequestLine("PATCH /v1/videos/{videoId}")
    fun updateVideo(
        @Param("videoId") videoId: String,
        @QueryMap updateVideoRequest: UpdateVideoRequest
    )

    @RequestLine("POST /v1/videos/{videoId}")
    fun createVideo(createVideoRequest: CreateVideoRequest): VideoResource

    @RequestLine("DELETE /v1/videos/{videoId}")
    fun deleteVideo(@Param("videoId") videoId: String)

    @RequestLine("PATCH /v1/videos/{videoId}?rating={rating}")
    fun updateVideoRating(@Param("videoId") videoId: String, @Param("rating") rating: Int)

    @RequestLine("PATCH /v1/videos/{videoId}?sharing=true")
    fun updateVideoSharing(@Param("videoId") videoId: String, @Param("sharing") sharing: Boolean)

    @RequestLine("GET /v1/videos/{videoId}/transcript")
    fun getVideoTranscript(@Param("videoId") videoId: String): String

    companion object {
        fun create(
            apiUrl: String,
            objectMapper: ObjectMapper = ObjectMapperDefinition.default(),
            tokenFactory: TokenFactory? = null
        ): VideosClient {
            return Feign.builder()
                .client(OkHttpClient())
                .encoder(JacksonEncoder(objectMapper))
                .decoder(JacksonDecoder(objectMapper))
                .requestInterceptor { template: RequestTemplate ->
                    if (tokenFactory != null) {
                        template.header("Authorization", "Bearer ${tokenFactory.getAccessToken()}")
                    }
                }
                .logLevel(Logger.Level.BASIC)
                .logger(Slf4jLogger())
                .target(VideosClient::class.java, apiUrl)
        }
    }
}

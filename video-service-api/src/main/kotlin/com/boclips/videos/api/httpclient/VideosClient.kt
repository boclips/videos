package com.boclips.videos.api.httpclient

import com.boclips.videos.api.httpclient.helper.ObjectMapperDefinition
import com.boclips.videos.api.request.video.AdminSearchRequest
import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.api.response.video.VideoResource
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
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.security.oauth2.client.OAuth2ClientContext

interface VideosClient {
    @RequestLine("GET /v1/videos/{videoId}")
    fun getVideo(@Param("videoId") videoId: String): Resource<VideoResource>

    @RequestLine("PATCH /v1/videos/{videoId}")
    fun updateVideo(@Param("videoId") videoId: String): Resource<Void>

    @RequestLine("DELETE /v1/videos/{videoId}")
    fun createVideo(@QueryMap createVideoRequest: CreateVideoRequest): Resource<VideoResource>

    @RequestLine("DELETE /v1/videos/{videoId}")
    fun deleteVideo(@Param("videoId") videoId: String): Resource<VideoResource>

    @RequestLine("PATCH /v1/videos/{videoId}?rating={rating}")
    fun updateVideoRating(@Param("videoId") videoId: String, @Param("rating") rating: String): Resource<Void>

    @RequestLine("PATCH /v1/videos/{videoId}?sharing=true")
    fun updateVideoSharing(@Param("videoId") videoId: String): Resource<Void>

    @RequestLine("GET /v1/videos/{videoId}/transcript")
    fun getVideoTranscript(@Param("videoId") videoId: String): String

    @RequestLine("POST /v1/search")
    fun searchVideos(@QueryMap searchRequest: AdminSearchRequest): Resources<VideoResource>

    companion object {
        fun create(
            apiUrl: String,
            objectMapper: ObjectMapper = ObjectMapperDefinition.default(),
            authContext: OAuth2ClientContext? = null
        ): VideosClient {
            return Feign.builder()
                .client(OkHttpClient())
                .encoder(JacksonEncoder(objectMapper))
                .decoder(JacksonDecoder(objectMapper))
                .requestInterceptor { template: RequestTemplate ->
                    if (authContext != null) {
                        if (template.headers().containsKey("Authorization")) {
                            template.header("Authorization", "Bearer ${authContext.accessTokenRequest.existingToken}")
                        }
                    }
                }
                .logLevel(Logger.Level.BASIC)
                .logger(Slf4jLogger())
                .target(VideosClient::class.java, apiUrl)
        }
    }
}

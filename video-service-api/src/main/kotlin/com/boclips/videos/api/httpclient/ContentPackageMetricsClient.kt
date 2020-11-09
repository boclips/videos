package com.boclips.videos.api.httpclient

import com.boclips.videos.api.httpclient.helper.ObjectMapperDefinition
import com.boclips.videos.api.httpclient.helper.TokenFactory
import com.boclips.videos.api.request.admin.VideosForContentPackageParams
import com.boclips.videos.api.response.video.VideoIdResource
import com.boclips.videos.api.response.video.VideoIdsResource
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

interface ContentPackageMetricsClient {
    @RequestLine("GET /v1/admin/actions/videos_for_content_package/{contentPackageId}")
    fun getVideosForContentPackage(
        @Param("contentPackageId") id: String,
        @QueryMap params: VideosForContentPackageParams = VideosForContentPackageParams()
    ): VideoIdsResource

    companion object {
        @JvmStatic
        fun create(
            apiUrl: String,
            objectMapper: ObjectMapper = ObjectMapperDefinition.default(),
            tokenFactory: TokenFactory? = null
        ): ContentPackageMetricsClient {
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
                .target(ContentPackageMetricsClient::class.java, apiUrl)
        }
    }
}

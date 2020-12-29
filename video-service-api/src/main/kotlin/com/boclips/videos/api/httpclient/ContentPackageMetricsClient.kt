package com.boclips.videos.api.httpclient

import com.boclips.videos.api.httpclient.helper.ObjectMapperDefinition
import com.boclips.videos.api.httpclient.helper.TokenFactory
import com.boclips.videos.api.request.admin.VideosForContentPackageParams
import com.boclips.videos.api.response.video.VideoIdsResource
import com.fasterxml.jackson.databind.ObjectMapper
import feign.*

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
            tokenFactory: TokenFactory? = null,
            feignClient: Client
        ) = FeignInterserviceClientFactory.create(
                apiUrl,
                objectMapper,
                tokenFactory,
                feignClient,
                ContentPackageMetricsClient::class.java
        )
    }
}

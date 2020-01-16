
package com.boclips.videos.api.httpclient

import com.boclips.videos.api.httpclient.helper.ObjectMapperDefinition
import com.boclips.videos.api.httpclient.helper.TokenFactory
import com.boclips.videos.api.request.contentpartner.ContentPartnerFilterRequest
import com.boclips.videos.api.request.contentpartner.CreateContentPartnerRequest
import com.boclips.videos.api.response.contentpartner.ContentPartnerResource
import com.boclips.videos.api.response.contentpartner.ContentPartnersResource
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

interface ContentPartnersClient {
    @RequestLine("GET /v1/content-partners")
    fun getContentPartners(@QueryMap contentPartnerFilterRequest: ContentPartnerFilterRequest = ContentPartnerFilterRequest()): ContentPartnersResource

    @RequestLine("GET /v1/content-partners/{contentPartnerId}")
    fun getContentPartner(@Param("contentPartnerId") contentPartnerId: String): ContentPartnerResource

    @RequestLine("POST /v1/content-partners")
    fun create(createContentPartnerRequest: CreateContentPartnerRequest)

    companion object {
        @JvmStatic
        fun create(
            apiUrl: String,
            objectMapper: ObjectMapper = ObjectMapperDefinition.default(),
            tokenFactory: TokenFactory? = null
        ): ContentPartnersClient {
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
                .target(ContentPartnersClient::class.java, apiUrl)
        }
    }
}

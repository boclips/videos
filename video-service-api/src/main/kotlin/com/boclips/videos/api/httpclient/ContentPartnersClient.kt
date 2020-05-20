package com.boclips.videos.api.httpclient

import com.boclips.videos.api.httpclient.helper.ObjectMapperDefinition
import com.boclips.videos.api.httpclient.helper.TokenFactory
import com.boclips.videos.api.request.channel.ChannelFilterRequest
import com.boclips.videos.api.request.channel.ChannelRequest
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.channel.LegacyContentPartnersResource
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
    fun getContentPartners(@QueryMap channelFilterRequest: ChannelFilterRequest = ChannelFilterRequest()): LegacyContentPartnersResource

    @RequestLine("GET /v1/content-partners/{contentPartnerId}")
    fun getContentPartner(@Param("contentPartnerId") contentPartnerId: String): ChannelResource

    @RequestLine("POST /v1/content-partners")
    fun create(upsertChannelRequest: ChannelRequest)

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

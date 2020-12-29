package com.boclips.videos.api.httpclient

import com.boclips.videos.api.httpclient.helper.ObjectMapperDefinition
import com.boclips.videos.api.httpclient.helper.TokenFactory
import com.boclips.videos.api.request.channel.ChannelFilterRequest
import com.boclips.videos.api.request.channel.ChannelRequest
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.channel.ChannelsResource
import com.fasterxml.jackson.databind.ObjectMapper
import feign.Client
import feign.Param
import feign.QueryMap
import feign.RequestLine

interface ChannelsClient {
    @RequestLine("GET /v1/channels")
    fun getChannels(@QueryMap channelFilterRequest: ChannelFilterRequest = ChannelFilterRequest()): ChannelsResource

    @RequestLine("GET /v1/channels/{channelId}")
    fun getChannel(@Param("channelId") channelId: String): ChannelResource

    @RequestLine("POST /v1/channels")
    fun create(upsertChannelRequest: ChannelRequest)

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
                ChannelsClient::class.java
        )
    }
}

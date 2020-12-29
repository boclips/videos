package com.boclips.videos.api.httpclient

import com.boclips.videos.api.httpclient.helper.ObjectMapperDefinition
import com.boclips.videos.api.httpclient.helper.TokenFactory
import com.fasterxml.jackson.databind.ObjectMapper
import feign.Client
import feign.Feign
import feign.Logger
import feign.RequestTemplate
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger

object FeignInterserviceClientFactory {

    fun <T> create(
        apiUrl: String,
        objectMapper: ObjectMapper = ObjectMapperDefinition.default(),
        tokenFactory: TokenFactory? = null,
        feignClient: Client,
        clientInterface: Class<T>
    ): T {
        return Feign.builder()
            .client(feignClient)
            .encoder(JacksonEncoder(objectMapper))
            .decoder(JacksonDecoder(objectMapper))
            .requestInterceptor { template: RequestTemplate ->
                if (tokenFactory != null) {
                    template.header("Authorization", "Bearer ${tokenFactory.getAccessToken()}")
                }
            }
            .logLevel(Logger.Level.BASIC)
            .logger(Slf4jLogger())
            .target(clientInterface, apiUrl)
    }
}

package com.boclips.videos.api.httpclient

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import feign.Feign
import feign.Logger
import feign.RequestTemplate
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger
import org.springframework.hateoas.hal.Jackson2HalModule
import org.springframework.security.oauth2.client.OAuth2ClientContext

class VideoServiceClientFactory {
    companion object {
        fun createVideosClient(apiUrl: String, authContext: OAuth2ClientContext? = null): VideosClient {
            val defaultObjectMapper = ObjectMapper()
            defaultObjectMapper.registerModule(Jackson2HalModule())
            defaultObjectMapper.registerModule(JavaTimeModule())
            defaultObjectMapper.registerModule(KotlinModule())
            defaultObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            return Feign.builder()
                .client(OkHttpClient())
                .encoder(JacksonEncoder(defaultObjectMapper))
                .decoder(JacksonDecoder(defaultObjectMapper))
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
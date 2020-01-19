package com.boclips.videos.api.httpclient.helper

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule

class ObjectMapperDefinition {
    companion object {
        @JvmStatic fun default(): ObjectMapper {
            val defaultObjectMapper = ObjectMapper()
            defaultObjectMapper.registerModule(Jackson2HalModule())
            defaultObjectMapper.registerModule(JavaTimeModule())
            defaultObjectMapper.registerModule(KotlinModule())
            defaultObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            return defaultObjectMapper
        }
    }
}

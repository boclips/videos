package com.boclips.videos.service.config

import com.boclips.web.EnableBoclipsApiErrors
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.context.annotation.Configuration
import org.springframework.hateoas.MediaTypes.HAL_JSON
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Duration

@Configuration
@EnableBoclipsApiErrors
class WebConfig : WebMvcConfigurer {
    /*
        This snippet configures Spring MVC message converters that deal with JSON serialization (using jackson).

        It adds HATEOAS support for every single JSON converter registered.

        The reason for doing this is that Spring was registering 2 of these and only 1 was getting HATEOAS
        config.
     */
    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.forEach {
            if (it is MappingJackson2HttpMessageConverter) {
                it.objectMapper.registerModule(Jackson2HalModule())
                it.objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
                it.supportedMediaTypes = listOf(HAL_JSON, HAL_JSON, MediaType.ALL)

                val customizations = SimpleModule(
                    "VideoServiceCustomizations",
                    Version(1, 0, 0, null, "com.boclips", "video-service")
                )
                customizations.addSerializer(Duration::class.java, DurationSerializer)
                it.objectMapper.registerModule(customizations)
            }
        }
    }
}

object DurationSerializer : JsonSerializer<Duration>() {
    override fun serialize(duration: Duration, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
        jsonGenerator.writeString(duration.toString())
    }
}

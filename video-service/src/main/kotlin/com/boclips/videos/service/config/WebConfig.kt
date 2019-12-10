package com.boclips.videos.service.config

import com.boclips.web.EnableBoclipsApiErrors
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.hateoas.MediaTypes.HAL_JSON
import org.springframework.hateoas.MediaTypes.HAL_JSON_UTF8
import org.springframework.hateoas.RelProvider
import org.springframework.hateoas.hal.HalConfiguration
import org.springframework.hateoas.hal.Jackson2HalModule
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Duration

@Configuration
@EnableBoclipsApiErrors
class WebConfig(
    val relProvider: RelProvider,
    val linkRelationMessageSource: MessageSourceAccessor
) : WebMvcConfigurer {

    /*
        This snippet configures Spring MVC message converters that deal with JSON serialization (using jackson).

        It adds HATEOAS support for every single JSON converter registered.

        The reason for doing this is that Spring was registering 2 of these and only 1 was getting HATEOAS
        config.
     */
    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.forEach {
            if (it is MappingJackson2HttpMessageConverter) {
                if (!Jackson2HalModule.isAlreadyRegisteredIn(it.objectMapper)) {
                    val instantiator = Jackson2HalModule.HalHandlerInstantiator(
                        relProvider, null,
                        linkRelationMessageSource, HalConfiguration()
                    )
                    it.objectMapper.registerModule(Jackson2HalModule())
                    it.objectMapper.setHandlerInstantiator(instantiator)
                    it.objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
                    it.objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                    it.supportedMediaTypes = listOf(HAL_JSON, HAL_JSON_UTF8, MediaType.ALL)

                    val customizations = SimpleModule("VideoServiceCustomizations", Version(1, 0, 0, null, "com.boclips", "video-service"))
                    customizations.addSerializer(Duration::class.java, DurationSerializer)
                    it.objectMapper.registerModule(customizations)
                }
            }
        }
    }
}

object DurationSerializer : JsonSerializer<Duration>() {
    override fun serialize(duration: Duration, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
        jsonGenerator.writeString(duration.toString())
    }
}

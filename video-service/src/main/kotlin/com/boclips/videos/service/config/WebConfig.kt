package com.boclips.videos.service.config

import com.boclips.web.EnableBoclipsApiErrors
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import mu.KLogging
import org.springframework.context.annotation.Configuration
import org.springframework.hateoas.MediaTypes.HAL_JSON
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Duration
import java.util.concurrent.Callable

@Configuration
@EnableBoclipsApiErrors
class WebConfig : WebMvcConfigurer {
    companion object : KLogging()
    /*
        This snippet configures Spring MVC message converters that deal with JSON serialization (using jackson).

        It adds HATEOAS support for every single JSON converter registered.

        The reason for doing this is that Spring was registering 2 of these and only 1 was getting HATEOAS
        config.
     */
    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.forEach {
            if (it is MappingJackson2HttpMessageConverter) {
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

    /*
    Async support is utilised for StreamingResponseBody which is used to download zip assets piped
    from good'ol kaltura.
     */
    override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
        configurer
            .setTaskExecutor(ThreadPoolTaskExecutor().apply {
                corePoolSize = 5
                maxPoolSize = 10
                setQueueCapacity(25)
            })
            .setDefaultTimeout(18000)
            .registerCallableInterceptors(object : TimeoutCallableProcessingInterceptor() {
                override fun <T> handleTimeout(request: NativeWebRequest?, task: Callable<T>?): Any? {
                    logger.error("Timeout during async processing: Boost timeout or reconsider strategy")
                    return super.handleTimeout(request, task)
                }
            })
    }
}

object DurationSerializer : JsonSerializer<Duration>() {
    override fun serialize(duration: Duration, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
        jsonGenerator.writeString(duration.toString())
    }
}

package com.boclips.videos.service.config.messaging

import com.boclips.events.config.BoclipsMessagingConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@EnableBinding(Topics::class, Subscriptions::class)
class MessagingContext(@Value("\${pubsub.topic.suffix}") val topicSuffix: String) {

    @Primary
    @Bean
    fun bindingServiceProperties() = BoclipsMessagingConfiguration(topicSuffix, "video-service").forContext(MessagingContext::class.java)
}

package com.boclips.videos.service.config

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.KalturaClientConfig
import com.boclips.videos.service.config.properties.KalturaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KalturaConfig {
    @Bean
    fun kalturaClient(kalturaProperties: KalturaProperties): KalturaClient = KalturaClient.create(KalturaClientConfig.builder()
            .partnerId(kalturaProperties.partnerId)
            .userId(kalturaProperties.userId)
            .secret(kalturaProperties.secret)
            .build())
}
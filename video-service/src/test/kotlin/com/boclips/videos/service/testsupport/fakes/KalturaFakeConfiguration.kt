package com.boclips.videos.service.testsupport.fakes

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.TestKalturaClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("fakes-kaltura")
@Configuration
class KalturaFakeConfiguration {
    @Bean
    fun fakeKalturaClient(): KalturaClient {
        return TestKalturaClient()
    }
}

package com.boclips.videos.service.testsupport.fakes

import com.boclips.videos.service.domain.service.SearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("fake-search")
@Configuration
class SearchServiceFakeConfiguration {
    @Bean
    @Primary
    fun fakeSearchService(): SearchService {
        return FakeSearchService()
    }
}
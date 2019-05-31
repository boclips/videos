package com.boclips.videos.service.testsupport.fakes

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.search.service.infrastructure.videos.InMemoryVideoReadSearchService
import com.nhaarman.mockito_kotlin.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("fake-search")
@Configuration
class SearchFakeConfiguration {

    @Bean
    fun videoMetadataSearchService() = InMemoryVideoReadSearchService()

    @Bean
    fun legacySearchService(): LegacySearchService {
        return mock()
    }
}

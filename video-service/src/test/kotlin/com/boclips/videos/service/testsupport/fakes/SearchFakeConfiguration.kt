package com.boclips.videos.service.testsupport.fakes

import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.search.service.infrastructure.contracts.InMemoryCollectionSearch
import com.boclips.search.service.infrastructure.contracts.InMemoryVideoSearch
import com.nhaarman.mockito_kotlin.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("fake-search")
@Configuration
class SearchFakeConfiguration {

    @Bean
    fun videoMetadataSearchService() = InMemoryVideoSearch()

    @Bean
    fun collectionMetadataSearchService() = InMemoryCollectionSearch()

    @Bean
    fun legacySearchService(): LegacyVideoSearchService {
        return mock()
    }
}

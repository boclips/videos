package com.boclips.videos.service.testsupport.fakes

import com.boclips.search.service.domain.SearchService
import com.boclips.search.service.infrastructure.InMemorySearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("fake-search")
@Configuration
class SearchFakeConfiguration {
    @Bean
    fun fakeSearchService(): SearchService {
        return InMemorySearchService()
    }
}

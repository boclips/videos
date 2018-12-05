package com.boclips.videos.service.testsupport.fakes

import com.boclips.search.service.domain.GenericSearchService
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.search.service.infrastructure.InMemorySearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("fake-search")
@Configuration
class SearchFakeConfiguration {
    @Bean
    fun fakeVideoMetadataSearchService(): GenericSearchService<VideoMetadata> {
        return InMemorySearchService()
    }
}

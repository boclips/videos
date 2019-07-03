package com.boclips.videos.service.testsupport.fakes

import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.search.service.infrastructure.fakes.CollectionSearchServiceFake
import com.boclips.search.service.infrastructure.fakes.VideoSearchServiceFake
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.infrastructure.search.DefaultCollectionSearch
import com.boclips.videos.service.infrastructure.search.DefaultVideoSearch
import com.nhaarman.mockito_kotlin.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("fakes-search")
@Configuration
class SearchContextFake {
    @Bean
    fun videoMetadataSearchService(): VideoSearchService {
        val inMemoryVideoSearch = VideoSearchServiceFake()
        return DefaultVideoSearch(inMemoryVideoSearch, inMemoryVideoSearch)
    }

    @Bean
    fun collectionMetadataSearchService(): CollectionSearchService {
        val inMemoryCollectionSearch = CollectionSearchServiceFake()
        return DefaultCollectionSearch(inMemoryCollectionSearch, inMemoryCollectionSearch)
    }

    @Bean
    fun legacySearchService(): LegacyVideoSearchService {
        return mock()
    }
}

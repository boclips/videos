package com.boclips.videos.service.testsupport.fakes

import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.search.service.infrastructure.contract.CollectionSearchServiceFake
import com.boclips.search.service.infrastructure.contract.VideoSearchServiceFake
import com.boclips.videos.service.domain.service.ContentPartnerService
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.infrastructure.search.DefaultCollectionSearch
import com.boclips.videos.service.infrastructure.search.DefaultVideoSearch
import com.nhaarman.mockitokotlin2.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("fakes-search")
@Configuration
class SearchContextFake {
    @Bean
    fun videoSearchFake(): VideoSearchServiceFake {
        return VideoSearchServiceFake()
    }

    @Bean
    fun videoMetadataSearchService(
        contentPartnerService: ContentPartnerService,
        videoSearchServiceFake: VideoSearchServiceFake
    ): VideoSearchService {
        return DefaultVideoSearch(
            videoSearchServiceFake,
            videoSearchServiceFake,
            contentPartnerService
        )
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

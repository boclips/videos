package com.boclips.videos.service.testsupport.fakes

import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.search.service.infrastructure.contract.CollectionIndexFake
import com.boclips.search.service.infrastructure.contract.VideoIndexFake
import com.boclips.videos.service.domain.service.VideoChannelService
import com.boclips.videos.service.domain.service.collection.CollectionIndex
import com.boclips.videos.service.domain.service.video.VideoIndex
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
    fun videoSearchFake(): VideoIndexFake {
        return VideoIndexFake()
    }

    @Bean
    fun videoMetadataSearchService(
        videoChannelService: VideoChannelService,
        videoIndexFake: VideoIndexFake
    ): VideoIndex {
        return DefaultVideoSearch(
            videoIndexFake,
            videoIndexFake,
            videoChannelService
        )
    }

    @Bean
    fun collectionIndexFake(): CollectionIndexFake {
        return CollectionIndexFake()
    }

    @Bean
    fun collectionMetadataSearchService(collectionIndexFake: CollectionIndexFake): CollectionIndex {
        return DefaultCollectionSearch(collectionIndexFake, collectionIndexFake)
    }

    @Bean
    fun legacySearchService(): LegacyVideoSearchService {
        return mock()
    }
}

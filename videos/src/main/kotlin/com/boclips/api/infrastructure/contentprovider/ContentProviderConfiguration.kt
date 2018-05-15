package com.boclips.api.infrastructure.contentprovider

import com.boclips.api.infrastructure.videos.VideoRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

@Configuration
class ContentProviderConfiguration {
    @Bean
    fun contentProviderService(
            videoRepository: VideoRepository,
            contentProviderRepository: ContentProviderRepository,
            mongoTemplate: ReactiveMongoTemplate
    ) = ContentProviderServiceImpl(videoRepository, contentProviderRepository, mongoTemplate)
}
package com.boclips.videos.service.testsupport.fakes

import com.boclips.videos.service.domain.model.playback.PlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("fake-youtube")
@Configuration
class YoutubeConfiguration {
    @Bean
    fun youtubePlaybackProvider(): PlaybackProvider {
        return TestYoutubePlaybackProvider()
    }
}

package com.boclips.videos.service.testsupport.fakes

import com.boclips.videos.service.domain.service.video.plackback.PlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("fakes-youtube")
@Configuration
class YoutubeConfiguration {
    @Bean
    fun youtubePlaybackProvider(): PlaybackProvider {
        return TestYoutubePlaybackProvider()
    }
}

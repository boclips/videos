package com.boclips.videos.service.testsupport.fakes

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.kalturaclient.media.streams.StreamUrls
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.Duration

@Profile("fake-kaltura")
@Configuration
class KalturaFakeConfiguration {
    @Bean
    @Primary
    fun fakeKalturaClient(): KalturaClient {
        val testKalturaClient = TestKalturaClient()
        testKalturaClient.addMediaEntry(mediaEntry("1"))
        testKalturaClient.addMediaEntry(mediaEntry("2"))
        testKalturaClient.addMediaEntry(mediaEntry("3"))
        testKalturaClient.addMediaEntry(mediaEntry("4"))
        testKalturaClient.addMediaEntry(mediaEntry("5"))
        return testKalturaClient
    }

    private fun mediaEntry(id: String): MediaEntry? {
        return MediaEntry.builder()
                .id(id)
                .referenceId("ref-id-$id")
                .streams(StreamUrls("https://stream/[FORMAT]/video-$id.mp4"))
                .thumbnailUrl("https://thumbnail/thumbnail-$id.mp4")
                .duration(Duration.ofMinutes(1))
                .build()
    }
}

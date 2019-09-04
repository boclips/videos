package com.boclips.videos.service.domain.service

import com.boclips.eventbus.domain.video.PlaybackProviderType
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

class EventConverterTest {
    @Test
    fun `creates a video event object`() {
        val id = TestFactories.aValidId()
        val video = TestFactories.createVideo(
            videoId = id,
            title = "the title",
            contentPartnerName = "the content partner",
            playback = TestFactories.createKalturaPlayback(duration = Duration.ofMinutes(2)),
            subjects = setOf(TestFactories.createSubject(name = "physics")),
            ageRange = AgeRange.bounded(5, 10)
        )

        val videoEvent = EventConverter().toVideoPayload(video)

        assertThat(videoEvent.id.value).isEqualTo(id)
        assertThat(videoEvent.title).isEqualTo("the title")
        assertThat(videoEvent.contentPartner.name).isEqualTo("the content partner")
        assertThat(videoEvent.playbackProviderType).isEqualTo(PlaybackProviderType.KALTURA)
        assertThat(videoEvent.subjects).hasSize(1)
        assertThat(videoEvent.subjects.first().name).isEqualTo("physics")
        assertThat(videoEvent.ageRange.min).isEqualTo(5)
        assertThat(videoEvent.ageRange.max).isEqualTo(10)
        assertThat(videoEvent.durationSeconds).isEqualTo(120)
    }

    @Test
    fun `sets correct playback provider type when YouTube`() {
        val video = TestFactories.createVideo(
                playback = TestFactories.createYoutubePlayback()
        )

        val videoEvent = EventConverter().toVideoPayload(video)

        assertThat(videoEvent.playbackProviderType).isEqualTo(PlaybackProviderType.YOUTUBE)
    }
}

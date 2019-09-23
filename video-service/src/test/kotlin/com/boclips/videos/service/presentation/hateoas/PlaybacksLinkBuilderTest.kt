package com.boclips.videos.service.presentation.hateoas

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Duration

internal class PlaybacksLinkBuilderTest {

    lateinit var linkBuilder: PlaybacksLinkBuilder

    @BeforeEach
    fun setup() {
        val fakeKalturaClient = TestKalturaClient()
        linkBuilder = PlaybacksLinkBuilder(fakeKalturaClient)
    }

    @Nested
    inner class ThumbnailUrl {
        @Test
        fun `it returns the playback thumbnail when kaltura`() {
            val playback = TestFactories.createKalturaPlayback(entryId = "thumbnail-entry-id")

            val link = linkBuilder.thumbnailLink(playback)

            assertThat(link).isNotNull
            assertThat(link!!.href).contains("thumbnail-entry-id")
            assertThat(link.href).contains("{thumbnailWidth}")
            assertThat(link.rel).isEqualTo("thumbnail")
        }

        @Test
        fun `it returns the playback thumbnail when youtube`() {
            val playback = TestFactories.createYoutubePlayback(thumbnailUrl = "expected-thumbnail-url")

            val link = linkBuilder.thumbnailLink(playback)

            assertThat(link).isNotNull
            assertThat(link!!.href).isEqualTo("expected-thumbnail-url")
            assertThat(link.rel).isEqualTo("thumbnail")
        }

        @Test
        fun `it returns null for a Faulty playback`() {
            val playback = VideoPlayback.FaultyPlayback(
                id = PlaybackId(type = PlaybackProviderType.KALTURA, value = "faulty"),
                duration = Duration.ZERO
            )

            val link = linkBuilder.thumbnailLink(playback)

            assertThat(link).isNull()
        }
    }

    @Nested
    inner class VideoPreviewUrl {
        @Test
        fun `it returns a video preview url for kaltura`() {
            val playback = TestFactories.createKalturaPlayback(entryId = "preview-entry-id")

            val link = linkBuilder.videoPreviewLink(playback)

            assertThat(link).isNotNull
            assertThat(link!!.href).contains("preview-entry-id")
            assertThat(link.href).contains("{thumbnailWidth}")
            assertThat(link.href).contains("{thumbnailCount}")
            assertThat(link.rel).isEqualTo("videoPreview")
        }

        @Test
        fun `it returns null for youtube`() {
            val playback = TestFactories.createYoutubePlayback()

            val link = linkBuilder.videoPreviewLink(playback)

            assertThat(link).isNull()
        }

        @Test
        fun `it returns null for a Faulty playback`() {
            val playback = VideoPlayback.FaultyPlayback(
                id = PlaybackId(type = PlaybackProviderType.KALTURA, value = "faulty"),
                duration = Duration.ZERO
            )

            val link = linkBuilder.videoPreviewLink(playback)

            assertThat(link).isNull()
        }
    }
}
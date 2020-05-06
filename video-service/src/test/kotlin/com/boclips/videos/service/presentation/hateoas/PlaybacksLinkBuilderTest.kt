package com.boclips.videos.service.presentation.hateoas

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
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
    inner class DownloadUrl {
        @Test
        fun `defined for Kaltura videos`() {
            setSecurityContext("someone@boclips.com", UserRoles.DOWNLOAD_VIDEO)

            val playback = TestFactories.createKalturaPlayback(downloadUrl = "https://download.me")

            val link = linkBuilder.downloadLink(playback)

            assertThat(link).isNotNull
            assertThat(link!!.href).isEqualTo("https://download.me")
            assertThat(link.rel).isEqualTo("download")
        }

        @Test
        fun `not defined for Kaltura videos when user unauthorized`() {
            setSecurityContext("someone@boclips.com")

            val playback = TestFactories.createKalturaPlayback(downloadUrl = "https://download.me")

            val link = linkBuilder.downloadLink(playback)

            assertThat(link).isNull()
        }

        @Test
        fun `not defined for YouTube videos`() {
            val playback = TestFactories.createYoutubePlayback()

            val link = linkBuilder.downloadLink(playback)

            assertThat(link).isNull()
        }
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

        @Test
        fun `it returns the thumbnail editor link when kaltura video and user can update video`() {
            setSecurityContext("editor", UserRoles.UPDATE_VIDEOS)
            val playback = TestFactories.createKalturaPlayback(entryId = "thumbnail-entry-id")

            val link = linkBuilder.editThumbnailLink(playback)

            assertThat(link).isNotNull
            assertThat(link!!.href).contains("thumbnail-entry-id/thumbnails")
            assertThat(link.rel).isEqualTo("editThumbnail")
        }

        @Test
        fun `no thumbnail editor link when youtube video`() {
            setSecurityContext("editor", UserRoles.UPDATE_VIDEOS)
            val playback = TestFactories.createYoutubePlayback()

            val link = linkBuilder.editThumbnailLink(playback)

            assertThat(link).isNull()
        }

        @Test
        fun `no thumbnail editor link when user does not have update video role`() {
            setSecurityContext("teacher", UserRoles.VIEW_VIDEOS)

            val playback = TestFactories.createKalturaPlayback(entryId = "thumbnail-entry-id")

            val link = linkBuilder.editThumbnailLink(playback)

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

    @Nested
    inner class HlsStreamUrl {
        @Test
        fun `it returns a hls stream url for kaltura`() {
            val playback = TestFactories.createKalturaPlayback(entryId = "preview-entry-id")

            val link = linkBuilder.hlsStreamLink(playback)

            assertThat(link).isNotNull
            assertThat(link!!.href).contains("format/applehttp")
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

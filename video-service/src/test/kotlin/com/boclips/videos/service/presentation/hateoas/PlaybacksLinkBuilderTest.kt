package com.boclips.videos.service.presentation.hateoas

import com.boclips.kalturaclient.clients.TestKalturaClient
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
        fun `it returns the playback thumbnail by second if set when kaltura`() {
            val playback = TestFactories.createKalturaPlayback(entryId = "thumbnail-entry-id", thumbnailSecond = 20)

            val link = linkBuilder.thumbnailLink(playback)

            assertThat(link).isNotNull
            assertThat(link!!.href).contains("thumbnail-entry-id")
            assertThat(link.href).contains("{thumbnailWidth}")
            assertThat(link.href).contains("/vid_sec/20")
            assertThat(link.rel).isEqualTo("thumbnail")
        }

        @Test
        fun `it returns the playback custom thumbnail if set when kaltura`() {
            val playback = TestFactories.createKalturaPlayback(entryId = "thumbnail-entry-id", customThumbnail = true)

            val link = linkBuilder.thumbnailLink(playback)

            assertThat(link).isNotNull
            assertThat(link!!.href).contains("thumbnail-entry-id")
            assertThat(link.href).contains("{thumbnailWidth}")
            assertThat(link.href).doesNotContain("/vid_sec")
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
            val video = TestFactories.createVideo(playback = playback)

            val setLink = linkBuilder.setThumbnailBySecond(playback, videoId = video.videoId)
            val deleteLink = linkBuilder.deleteThumbnail(playback, videoId = video.videoId)

            assertThat(deleteLink).isNull()
            assertThat(setLink).isNotNull
            assertThat(setLink!!.href).contains("/v1/videos/${video.videoId.value}/playback{?thumbnailSecond}")
            assertThat(setLink.rel).isEqualTo("setThumbnailBySecond")
        }

        @Test
        fun `it returns the custom thumbnail link when kaltura video and user can update video`() {
            setSecurityContext("editor", UserRoles.UPDATE_VIDEOS)
            val playback = TestFactories.createKalturaPlayback(entryId = "thumbnail-entry-id")
            val video = TestFactories.createVideo(playback = playback)

            val setLink = linkBuilder.setCustomThumbnail(playback, videoId = video.videoId)
            val deleteLink = linkBuilder.deleteThumbnail(playback, videoId = video.videoId)

            assertThat(deleteLink).isNull()
            assertThat(setLink).isNotNull
            assertThat(setLink!!.href).contains("/v1/videos/${video.videoId.value}/playback{?playbackId,thumbnailImage}")
            assertThat(setLink.rel).isEqualTo("setCustomThumbnail")
        }

        @Test
        fun `it returns the delete thumbnail link when thumbnail second was manually set`() {
            setSecurityContext("editor", UserRoles.UPDATE_VIDEOS)
            val playback = TestFactories.createKalturaPlayback(entryId = "thumbnail-entry-id", thumbnailSecond = 20)
            val video = TestFactories.createVideo(playback = playback)

            val setThumbnailSecondLink = linkBuilder.setThumbnailBySecond(playback, videoId = video.videoId)
            val setCustomThumbnailLink = linkBuilder.setCustomThumbnail(playback, videoId = video.videoId)
            val deleteLink = linkBuilder.deleteThumbnail(playback, videoId = video.videoId)

            assertThat(setCustomThumbnailLink).isNull()
            assertThat(setThumbnailSecondLink).isNull()
            assertThat(deleteLink).isNotNull
            assertThat(deleteLink!!.href).contains("/v1/videos/${video.videoId.value}/playback/thumbnail")
            assertThat(deleteLink.rel).isEqualTo("deleteThumbnail")
        }

        @Test
        fun `it returns the delete thumbnail link when custom thumbnail was manually set`() {
            setSecurityContext("editor", UserRoles.UPDATE_VIDEOS)
            val playback = TestFactories.createKalturaPlayback(entryId = "thumbnail-entry-id", customThumbnail = true)
            val video = TestFactories.createVideo(playback = playback)

            val setThumbnailSecondLink = linkBuilder.setThumbnailBySecond(playback, videoId = video.videoId)
            val setCustomThumbnailLink = linkBuilder.setCustomThumbnail(playback, videoId = video.videoId)
            val deleteLink = linkBuilder.deleteThumbnail(playback, videoId = video.videoId)

            assertThat(setThumbnailSecondLink).isNull()
            assertThat(setCustomThumbnailLink).isNull()
            assertThat(deleteLink).isNotNull
            assertThat(deleteLink!!.href).contains("/v1/videos/${video.videoId.value}/playback/thumbnail")
            assertThat(deleteLink.rel).isEqualTo("deleteThumbnail")
        }

        @Test
        fun `no thumbnail editor link when youtube video`() {
            setSecurityContext("editor", UserRoles.UPDATE_VIDEOS)
            val playback = TestFactories.createYoutubePlayback()
            val video = TestFactories.createVideo(playback = playback)

            val thumbnailBySecondLink = linkBuilder.setThumbnailBySecond(playback, videoId = video.videoId)
            val customThumbnailLink = linkBuilder.setCustomThumbnail(playback, videoId = video.videoId)

            assertThat(thumbnailBySecondLink).isNull()
            assertThat(customThumbnailLink).isNull()
        }

        @Test
        fun `no thumbnail editor link when user does not have update video role`() {
            setSecurityContext("teacher", UserRoles.VIEW_VIDEOS)

            val playback = TestFactories.createKalturaPlayback(entryId = "thumbnail-entry-id")
            val video = TestFactories.createVideo(playback = playback)

            val thumbnailBySecondLink = linkBuilder.setThumbnailBySecond(playback, videoId = video.videoId)
            val customThumbnailLink = linkBuilder.setCustomThumbnail(playback, videoId = video.videoId)

            assertThat(thumbnailBySecondLink).isNull()
            assertThat(customThumbnailLink).isNull()
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

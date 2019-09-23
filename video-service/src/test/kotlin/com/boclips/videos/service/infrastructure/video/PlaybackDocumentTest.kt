package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PlaybackDocumentTest {

    @Nested
    @DisplayName("Kaltura documents")
    inner class KalturaDocuments {
        @Test
        fun `id needs to exist`() {
            val playbackDocument = TestFactories.createKalturaPlaybackDocument(
                id = "",
                hlsStreamUrl = null
            )

            assertThat(playbackDocument.isCompleteKalturaPlayback()).isFalse()
        }

        @Test
        fun `entryId needs to exist`() {
            val playbackDocument = TestFactories.createKalturaPlaybackDocument(
                entryId = ""
            )

            assertThat(playbackDocument.isCompleteKalturaPlayback()).isFalse()
        }

        @Test
        fun `hlsStreamUrl needs to be set`() {
            assertThat(
                TestFactories.createKalturaPlaybackDocument(
                    hlsStreamUrl = null
                ).isCompleteKalturaPlayback()
            ).isFalse()
        }

        @Test
        fun `dashStreamUrl needs to be set`() {
            assertThat(
                TestFactories.createKalturaPlaybackDocument(
                    dashStreamUrl = null
                ).isCompleteKalturaPlayback()
            ).isFalse()
        }

        @Test
        fun `downloadUrl needs to be set`() {
            assertThat(
                TestFactories.createKalturaPlaybackDocument(
                    downloadUrl = null
                ).isCompleteKalturaPlayback()
            ).isFalse()
        }

        @Test
        fun `progressive streamUrl needs to be set`() {
            assertThat(
                TestFactories.createKalturaPlaybackDocument(
                    progressiveStreamUrl = null
                ).isCompleteKalturaPlayback()
            ).isFalse()
        }

        @Test
        fun `duration needs to be set`() {
            assertThat(
                TestFactories.createKalturaPlaybackDocument(
                    duration = null
                ).isCompleteKalturaPlayback()
            ).isFalse()
        }
    }

    @Nested
    @DisplayName("Youtube documents")
    inner class YoutubeDocuments {
        @Test
        fun `id needs to be set`() {
            assertThat(
                TestFactories.createYoutubePlaybackDocument(
                    duration = null
                ).isCompleteYoutubePlayback()
            ).isFalse()
        }

        @Test
        fun `thumbnails needs to be set`() {
            assertThat(
                TestFactories.createYoutubePlaybackDocument(
                    duration = null
                ).isCompleteYoutubePlayback()
            ).isFalse()
        }

        @Test
        fun `duration needs to be set`() {
            assertThat(
                TestFactories.createYoutubePlaybackDocument(
                    duration = null
                ).isCompleteYoutubePlayback()
            ).isFalse()
        }
    }
}
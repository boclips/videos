package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.playback.Dimensions
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.VideoAsset
import com.boclips.videos.service.infrastructure.video.PlaybackDocument
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.VideoFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

class PlaybackConverterTest {

    @Test
    fun `converting from a Kaltura playback to a document`() {
        val originalPlayback: VideoPlayback.StreamPlayback = TestFactories.createKalturaPlayback(
            entryId = "entry_id_1234",
            referenceId = "1234",
            duration = Duration.ofSeconds(100),
            downloadUrl = "download",
            thumbnailSecond = 24,
            customThumbnail = true,
            assets = setOf(VideoFactory.createVideoAsset()),
            originalDimensions = Dimensions(
                width = 320,
                height = 480
            )
        )

        val playbackDocument: PlaybackDocument = PlaybackConverter.toDocument(originalPlayback)
        assertThat(playbackDocument.id).isEqualTo("1234")
        assertThat(playbackDocument.entryId).isEqualTo("entry_id_1234")
        assertThat(playbackDocument.thumbnailUrl).isNull()
        assertThat(playbackDocument.thumbnailSecond).isEqualTo(24)
        assertThat(playbackDocument.customThumbnail).isTrue()
        assertThat(playbackDocument.downloadUrl).isEqualTo("download")
        assertThat(playbackDocument.duration).isEqualTo(100)
        assertThat(playbackDocument.lastVerified).isNotNull()
        assertThat(playbackDocument.assets).hasSize(1)
        assertThat(playbackDocument.originalWidth).isEqualTo(320)
        assertThat(playbackDocument.originalHeight).isEqualTo(480)
    }

    @Test
    fun `converting from a Kaltura document to a playback`() {
        val document = TestFactories.createKalturaPlaybackDocument(
            id = "1234",
            entryId = "entry_id_1234",
            duration = 100,
            downloadUrl = "download",
            thumbnailSecond = 35,
            customThumbnail = true,
            assets = listOf(
                VideoFactory.createVideoAssetDocument(
                    id = "the_asset_id",
                    sizeKb = 100,
                    width = 200,
                    height = 300,
                    bitrateKbps = 400
                )
            ),
            originalWidth = 200,
            originalHeight = 300
        )

        val playback = PlaybackConverter.toPlayback(document) as VideoPlayback.StreamPlayback

        assertThat(playback.id.value).isEqualTo("entry_id_1234")
        assertThat(playback.referenceId).isEqualTo("1234")
        assertThat(playback.downloadUrl).isEqualTo("download")
        assertThat(playback.thumbnailSecond).isEqualTo(35)
        assertThat(playback.customThumbnail).isTrue()
        assertThat(playback.duration).isEqualTo(Duration.ofSeconds(100))
        assertThat(playback.assets?.first()).isEqualTo(
            VideoAsset(
                reference = "the_asset_id",
                sizeKb = 100,
                dimensions = Dimensions(
                    width = 200,
                    height = 300
                ),
                bitrateKbps = 400
            )
        )
        assertThat(playback.originalDimensions?.height).isEqualTo(300)
        assertThat(playback.originalDimensions?.width).isEqualTo(200)
    }

    @Test
    fun `converting from Kaltura document to a playback when no thumbnailSec and no customThumbnail`() {
        val document = TestFactories.createKalturaPlaybackDocument(
            downloadUrl = "download-url",
            thumbnailSecond = null,
            customThumbnail = false,
            duration = 10
        )

        val playback = PlaybackConverter.toPlayback(document) as VideoPlayback.StreamPlayback

        assertThat(playback.thumbnailSecond).isNull()
        assertThat(playback.customThumbnail).isFalse()
    }

    @Test
    fun `converting from Kaltura document to a playback when no original dimensions`() {
        val document = TestFactories.createKalturaPlaybackDocument(
            originalWidth = null,
            originalHeight = null,
            downloadUrl = "download-url",
            duration = 10
        )

        val playback = PlaybackConverter.toPlayback(document) as VideoPlayback.StreamPlayback

        assertThat(playback.originalDimensions).isNull()
    }

    @Test
    fun `convert Youtube playback to document, and back again`() {
        val originalPlayback = TestFactories.createYoutubePlayback(
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "some-yt-id"),
            duration = Duration.ofSeconds(100),
            thumbnailUrl = "thumbnail-url"
        )

        val playbackDocument = PlaybackConverter.toDocument(originalPlayback)
        assertThat(playbackDocument.id).isEqualTo("some-yt-id")
        assertThat(playbackDocument.thumbnailUrl).containsExactly("thumbnail-url")
        assertThat(playbackDocument.entryId).isNull()
        assertThat(playbackDocument.downloadUrl).isNull()
        assertThat(playbackDocument.duration).isEqualTo(100)
        assertThat(playbackDocument.lastVerified).isNotNull()

        val convertedPlayback = PlaybackConverter.toPlayback(playbackDocument)
        assertThat(convertedPlayback).isEqualTo(originalPlayback)
    }
}

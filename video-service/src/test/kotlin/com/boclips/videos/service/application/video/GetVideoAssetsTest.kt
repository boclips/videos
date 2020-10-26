package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.CaptionFormat
import com.boclips.videos.service.domain.service.video.CaptionService
import com.boclips.videos.service.domain.service.video.plackback.PlaybackProvider
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.zip.ZipInputStream

class GetVideoAssetsTest {

    val captionService = mock<CaptionService>()
    val searchVideo = mock<SearchVideo>()
    val playbackProvider = mock<PlaybackProvider>()
    val getVideoAssets = GetVideoAssets(captionService, searchVideo, playbackProvider)

    @Test
    fun `returns appropriate filename`() {
        val filename = GetVideoAssets.buildFilename(
            "!@#$%^&*()a $ great title9..."
        )

        assertThat(filename).isEqualTo("a-great-title9")
    }

    @Test
    fun `returns captions & video content`() {
        whenever(playbackProvider.downloadHighestResolutionVideo(eq(PlaybackId.from("entry-id", "KALTURA")), any()))
            .then { invocation -> (invocation.arguments[1] as OutputStream).write("movie time!".toByteArray()) }
        whenever(playbackProvider.getExtensionForAsset(eq(PlaybackId.from("entry-id", "KALTURA")))).thenReturn("mov")
        val baos = ByteArrayOutputStream()
        getVideoAssets.writeCompressedContent(
            outputStream = baos,
            video = TestFactories.createVideo(title = "a great title 9*", playback = TestFactories.createKalturaPlayback(entryId = "entry-id")),
            caption = Caption(content = "caption time!", format = CaptionFormat.SRT, default = true)
        )

        val zipInputStream = unzipFirstEntry(baos)
        assertThat(zipInputStream.nextEntry.name).isEqualTo("a-great-title-9.srt")
        assertThat(zipInputStream.readBytes()).isEqualTo("caption time!".toByteArray())

        assertThat(zipInputStream.nextEntry.name).isEqualTo("a-great-title-9.mov")
        assertThat(zipInputStream.readBytes()).isEqualTo("movie time!".toByteArray())
    }

    @Test
    fun `returns only video content`() {
        whenever(playbackProvider.downloadHighestResolutionVideo(eq(PlaybackId.from("entry-id", "KALTURA")), any()))
            .then { invocation -> (invocation.arguments[1] as OutputStream).write("movie time!".toByteArray()) }

        whenever(playbackProvider.getExtensionForAsset(eq(PlaybackId.from("entry-id", "KALTURA")))).thenReturn("mov")

        val baos = ByteArrayOutputStream()

        getVideoAssets.writeCompressedContent(
            outputStream = baos,
            video = TestFactories.createVideo(title = "a great title 9*", playback = TestFactories.createKalturaPlayback(entryId = "entry-id")),
            caption = null
        )

        val zipInputStream = unzipFirstEntry(baos)

        assertThat(zipInputStream.nextEntry.name).isEqualTo("a-great-title-9.mov")
        assertThat(zipInputStream.readBytes()).isEqualTo("movie time!".toByteArray())
    }

    private fun unzipFirstEntry(baos: ByteArrayOutputStream) = ZipInputStream(ByteArrayInputStream(baos.toByteArray()))
}

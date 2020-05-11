package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.CaptionFormat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater
import java.util.zip.ZipInputStream

class GetVideoAssetsTest {

    @Test
    fun `returns appropriate filename`() {
        val filename = GetVideoAssets.buildFilename(
            "!@#$%^&*()a $ great title9..."
        )

        assertThat(filename).isEqualTo("a-great-title9")
    }

    @Test
    fun `returns appropriate content`() {
        val baos = ByteArrayOutputStream()
        GetVideoAssets.writeCompressedContent(
            outputStream = baos, title = "a great title 9*",
            caption = Caption(content = "content!", format = CaptionFormat.SRT)
        )

        val zipInputStream = ZipInputStream(ByteArrayInputStream(baos.toByteArray()))
        val nextEntry = zipInputStream.nextEntry
        assertThat(nextEntry.name).isEqualTo("a-great-title-9.srt")
        assertThat(zipInputStream.readBytes()).isEqualTo("content!".toByteArray())
    }

}
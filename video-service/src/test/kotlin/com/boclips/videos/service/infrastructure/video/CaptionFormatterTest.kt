package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.video.CaptionFormat
import com.boclips.videos.service.domain.model.video.UnsupportedFormatConversionException
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CaptionFormatterTest {
    // Trim ident was causing issues by removing the last line which is need for a valid SRT
    val vttCaptionContent = """WEBVTT

00:00:00.500 --> 00:00:02.000
The Web is always changing

00:00:02.500 --> 00:00:04.300
and the way we access it is changing

"""

    val vttCaptionContentWithIds = """WEBVTT

1
00:00:00.500 --> 00:00:02.000
The Web is always changing

2
00:00:02.500 --> 00:00:04.300
and the way we access it is changing

"""

    val srtCaptionContent = """
1
00:00:00,500 --> 00:00:02,000
The Web is always changing

2
00:00:02,500 --> 00:00:04,300
and the way we access it is changing

"""

    val captionFormatter = NoopHQCaptionConverter()

    @Test
    fun `from srt to vtt`() {
        val convertedVttCaption =
            captionFormatter.convert(content = srtCaptionContent, from = CaptionFormat.SRT, to = CaptionFormat.WEBVTT)

        assertThat(normalizeContent(convertedVttCaption)).isEqualTo(normalizeContent(vttCaptionContentWithIds))
    }

    @Test
    fun `from vtt to srt`() {
        val convertedSrtContent =
            captionFormatter.convert(content = vttCaptionContent, from = CaptionFormat.WEBVTT, to = CaptionFormat.SRT)

        assertThat(normalizeContent(convertedSrtContent)).isEqualTo(normalizeContent(srtCaptionContent))
    }

    @Test
    fun `from srt to srt`() {
        val convertedSrtContent =
            captionFormatter.convert(content = srtCaptionContent, from = CaptionFormat.SRT, to = CaptionFormat.SRT)

        assertThat(normalizeContent(convertedSrtContent)).isEqualTo(normalizeContent(srtCaptionContent))
    }

    @Test
    fun `from vtt to vtt`() {
        val convertedVttContent =
            captionFormatter.convert(
                content = vttCaptionContent,
                from = CaptionFormat.WEBVTT,
                to = CaptionFormat.WEBVTT
            )

        assertThat(normalizeContent(convertedVttContent)).isEqualTo(normalizeContent(vttCaptionContent))
    }

    @Test
    fun `from an unsupported format`() {
        assertThrows<UnsupportedFormatConversionException> {
            captionFormatter.convert("some format", from = CaptionFormat.DFXP, to = CaptionFormat.WEBVTT)
        }
    }

    @Test
    fun `to an unsupported format`() {
        assertThrows<UnsupportedFormatConversionException> {
            captionFormatter.convert(srtCaptionContent, from = CaptionFormat.SRT, to = CaptionFormat.CAP)
        }
    }

    private fun normalizeContent(content: String) = content.replace("\n", "").replace(" ", "")
}

package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.CaptionFormat
import com.boclips.videos.service.domain.model.video.UnsupportedFormatConversionException
import com.boclips.videos.service.infrastructure.captions.NomalabCaptionConverter
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NomalabCaptionConverterTest {
    // Trim ident was causing issues by removing the last line which is need for a valid SRT
    private val vttCaptionContent = """WEBVTT

00:00:00.500 --> 00:00:02.000
The Web is always changing

00:00:02.500 --> 00:00:04.300
and the way we access it is changing

"""

    private val vttCaptionContentWithCues = """WEBVTT

1
00:00:00.500 --> 00:00:02.000
The Web is always changing

2
00:00:02.500 --> 00:00:04.300
and the way we access it is changing

"""

    private val srtCaptionContent = """
1
00:00:00,500 --> 00:00:02,000
The Web is always changing

2
00:00:02,500 --> 00:00:04,300
and the way we access it is changing

"""

    private val captionFormatter = NomalabCaptionConverter()

    @Test
    fun `from vtt with cues format to srt`() {
        captionFormatter.convert(vttCaptionContentWithCues, from = CaptionFormat.WEBVTT, to = CaptionFormat.SRT)
    }

    @Test
    fun `from srt to vtt`() {
        val convertedVttCaption =
            captionFormatter.convert(content = srtCaptionContent, from = CaptionFormat.SRT, to = CaptionFormat.WEBVTT)

        assertThat(normalizeContent(convertedVttCaption)).isEqualTo(normalizeContent(vttCaptionContentWithCues))
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

    @Test
    fun `convert SRT captions to transcript`() {
        val expectedTranscript = """The Web is always changing
and the way we access it is changing"""

        assertThat(
            captionFormatter.convertToTranscript(
                Caption(
                    content = srtCaptionContent,
                    isHumanGenerated = true,
                    format = CaptionFormat.SRT,
                    default = true
                )
            )
        ).isEqualTo(expectedTranscript)
    }

    @Test
    fun `convert VTT captions to transcript`() {
        val expectedTranscript = """The Web is always changing
and the way we access it is changing"""

        assertThat(
            captionFormatter.convertToTranscript(
                Caption(
                    content = vttCaptionContent,
                    isHumanGenerated = true,
                    format = CaptionFormat.WEBVTT,
                    default = true
                )
            )
        ).isEqualTo(expectedTranscript)
    }

    @Test
    fun `throws error when converting CAP captions to transcript`() {
        assertThrows<UnsupportedFormatConversionException> {
            (
                captionFormatter.convertToTranscript(
                    Caption(
                        content = vttCaptionContent,
                        isHumanGenerated = true,
                        format = CaptionFormat.CAP,
                        default = true
                    )
                )
                )
        }
    }

    private fun normalizeContent(content: String) = content.replace("\n", "").replace(" ", "")
}

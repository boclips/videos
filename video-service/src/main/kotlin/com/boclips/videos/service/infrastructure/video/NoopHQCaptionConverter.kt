package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.CaptionFormat
import com.boclips.videos.service.domain.model.video.UnsupportedFormatConversionException
import com.boclips.videos.service.domain.service.video.CaptionConverter
import fr.noop.subtitle.model.SubtitleParser
import fr.noop.subtitle.srt.SrtParser
import fr.noop.subtitle.srt.SrtWriter
import fr.noop.subtitle.vtt.VttParser
import fr.noop.subtitle.vtt.VttWriter
import java.io.ByteArrayOutputStream

class NoopHQCaptionConverter : CaptionConverter {

    override fun convert(content: String, from: CaptionFormat, to: CaptionFormat): String {
        if (from == to) {
            return content
        }

        val captionObject = getParser(from).parse(content.byteInputStream())

        val writer = when (to) {
            CaptionFormat.SRT -> SrtWriter("utf-8")
            CaptionFormat.WEBVTT -> VttWriter("utf-8")
            else -> throw UnsupportedFormatConversionException("Cannot convert to format of $to")
        }

        ByteArrayOutputStream().let { outputStream ->
            writer.write(captionObject, outputStream)
            return outputStream.toString()
        }
    }

    override fun convertToTranscript(caption: Caption): String {
        val captionObject = getParser(caption.format).parse(caption.content.byteInputStream())

        return captionObject.cues.joinToString("\n") { it.text }
    }

    private fun getParser(format: CaptionFormat): SubtitleParser {
        return when (format) {
            CaptionFormat.SRT -> SrtParser("utf-8")
            CaptionFormat.WEBVTT -> VttParser("utf-8")
            else -> throw UnsupportedFormatConversionException("Cannot parse from format of $format")
        }
    }
}

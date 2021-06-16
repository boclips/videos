package com.boclips.videos.service.infrastructure.captions

import com.boclips.videos.service.application.collection.exceptions.InvalidWebVTTException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ExoWebVTTValidatorTest {
    private val exoWebVTTParser: ExoWebVTTValidator = ExoWebVTTValidator()
    val validWebVTTContent = """WEBVTT FILE

                        1
                        00:01.981 --> 00:04.682
                        We're quite content to be the odd<br>browser out.

                        2
                        00:05.302 --> 00:08.958
                        We don't have a fancy stock abbreviation <br>to go alongside our name in the press.

                        3
                        00:09.526 --> 00:11.324
                        We don't have a profit margin.
    """.trimIndent()

    val validWebVTTContentWithNotes = """WEBVTT\r\n\r\nNOTE Confidence: 0.8984\r\n\r\n00:00:00.000 --> 00:00:04.060\r\nWhile regaling you with daring stories from her youth,\r\n\r\nNOTE Confidence: 0.8984\r\n\r\n00:00:04.060 --> 00:00:07.080\r\nit might be hard to believe your\r\n\r\nNOTE Confidence: 0.8984\r\n\r\n00:00:07.080 --> 00:00:10.110\r\ngrandmother used to be a trapeze artist.\r\n\r\n""".trimIndent()

    @Test
    fun `validates correct caption content`() {
        assertThat(exoWebVTTParser.checkValid(validWebVTTContent)).isEqualTo(true)
        assertThat(exoWebVTTParser.checkValid(validWebVTTContentWithNotes)).isEqualTo(true)
    }

    @Test
    fun `invalidates incorrect caption content`() {
        assertThrows<InvalidWebVTTException> { exoWebVTTParser.checkValid("not a valid webvtt file") }
    }

    @Test
    fun `parses a transcript from valid webvtt captions and excludes notes`() {
        val transcript = exoWebVTTParser.parse(validWebVTTContentWithNotes)

        assertThat(transcript).hasSize(3)
        assertThat(transcript[0]).isEqualTo("While regaling you with daring stories from her youth,")
        assertThat(transcript[1]).isEqualTo("it might be hard to believe your")
        assertThat(transcript[2]).isEqualTo("grandmother used to be a trapeze artist.")
    }
}

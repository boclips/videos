package com.boclips.videos.service.domain.model.video

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CaptionFormatTest {

    @Test
    fun `returns appropriate file extensions`() {
        assertThat(CaptionFormat.DFXP.getFileExtension()).isEqualTo("dfxp")
        assertThat(CaptionFormat.WEBVTT.getFileExtension()).isEqualTo("vtt")
        assertThat(CaptionFormat.SRT.getFileExtension()).isEqualTo("srt")
        assertThat(CaptionFormat.CAP.getFileExtension()).isEqualTo("cap")
    }
}

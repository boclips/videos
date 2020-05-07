package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.CaptionFormat
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GetVideoAssetsTest {

    @Test
    fun `returns appropriate filename`() {
        val filename = GetVideoAssets.buildFilename(
            TestFactories.createVideo(title = "!@#$%^&*()a $ great title9..."),
            Caption(content = "", format = CaptionFormat.SRT)
        )

        assertThat(filename).isEqualTo("a-great-title9.srt")
    }
}
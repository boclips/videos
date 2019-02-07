package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.service.VideoSubjectsUpdate
import com.boclips.videos.service.domain.model.asset.Subject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoResourceToVideoUpdateConverterTest {

    @Test
    fun `no update`() {
        val videoResource = VideoResource()
        val videoUpdate = VideoResourceToVideoUpdateConverter.convert(videoResource)
        assertThat(videoUpdate).isEmpty()
    }

    @Test
    fun `convert video resource to video update`() {
        val videoResource = VideoResource(subjects = setOf("Maths"))
        val videoUpdate = VideoResourceToVideoUpdateConverter.convert(videoResource)
        assertThat(videoUpdate).containsExactly(VideoSubjectsUpdate(setOf(Subject("Maths"))))
    }
}
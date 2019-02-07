package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.service.VideoSubjectsUpdate
import com.boclips.videos.service.domain.model.asset.Subject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoResourceToVideoUpdateConverterTest {

    @Test
    fun `no subjects to be updated`() {
        val videoResource = VideoResource()
        val videoUpdate = VideoResourceToVideoUpdateConverter.convert(videoResource)
        assertThat(videoUpdate.subjects).isEmpty()
    }

    @Test
    fun `convert video resource to video update for subjects`() {
        val videoResource = VideoResource(subjects = setOf("Maths"))
        val videoUpdate = VideoResourceToVideoUpdateConverter.convert(videoResource)
        assertThat(videoUpdate).isEqualTo(VideoSubjectsUpdate(listOf(Subject("Maths"))))
    }
}
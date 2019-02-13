package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.asset.Subject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoResourceToVideoUpdateConverterTest {

    @Test
    fun `no subjects to be updated`() {
        val videoResource = VideoResource()
        val update = VideoResourceToPartialVideoAssetConverter.convert(videoResource)

        assertThat(update.subjects).isNull()
    }

    @Test
    fun `convert video resource to video update for subjects`() {
        val videoResource = VideoResource(subjects = setOf("Maths"))
        val update = VideoResourceToPartialVideoAssetConverter.convert(videoResource)

        assertThat(update.subjects).isEqualTo(setOf(Subject("Maths")))
    }
}
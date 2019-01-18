package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.service.VideoSubjectsUpdate
import com.boclips.videos.service.domain.service.VideoTitleUpdate
import com.boclips.videos.service.domain.service.VideoUpdateCommand
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoUpdateCommandTest {

    @Test
    fun `updates can be combined`() {
        val update = VideoUpdateCommand.combine(listOf(
                VideoSubjectsUpdate(setOf(Subject("Maths"))),
                VideoTitleUpdate("new title")
        ))

        val video = TestFactories.createVideo(
                videoAsset = TestFactories.createVideoAsset(title = "", subjects = emptySet())
        )

        val updatedVideo = update.update(video)

        assertThat(updatedVideo.asset.subjects).containsExactly(Subject("Maths"))
        assertThat(updatedVideo.asset.title).isEqualTo("new title")
    }
}
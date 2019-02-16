package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.asset.IllegalVideoIdentifierException
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.service.video.ReplaceSubjects
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VideoUpdatesConverterTest {

    @Test
    fun `throws when video asset id is invalid`() {
        val videoResource = VideoResource(id = "")

        assertThrows<IllegalVideoIdentifierException> {
            VideoUpdatesConverter.convert(videoResource)
        }
    }

    @Test
    fun `no subjects to be updated`() {
        val videoResource = VideoResource(id = TestFactories.aValidId())
        val updates = VideoUpdatesConverter.convert(videoResource)

        assertThat(updates).isEmpty()
    }

    @Test
    fun `convert video resource to video update for subjects`() {
        val videoResource = VideoResource(id = TestFactories.aValidId(), subjects = setOf("Maths"))
        val updates = VideoUpdatesConverter.convert(videoResource)

        val replaceSubjects = updates.first() as ReplaceSubjects

        assertThat(replaceSubjects.subjects).isEqualTo(listOf(Subject("Maths")))
    }
}
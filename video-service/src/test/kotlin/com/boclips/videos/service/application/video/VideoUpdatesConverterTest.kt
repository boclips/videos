package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoUpdatesConverterTest {

    @Test
    fun `no subjects to be updated`() {
        val assetId = AssetId(value = TestFactories.aValidId())
        val videoResource = VideoResource(id = TestFactories.aValidId())

        val updates = VideoUpdatesConverter.convert(assetId, videoResource)

        assertThat(updates).isEmpty()
    }

    @Test
    fun `convert video resource to video update for subjects`() {
        val assetId = AssetId(value = TestFactories.aValidId())
        val videoResource = VideoResource(id = TestFactories.aValidId(), subjects = setOf("Maths"))

        val updates = VideoUpdatesConverter.convert(assetId, videoResource)

        val replaceSubjects = updates.first() as VideoUpdateCommand.ReplaceSubjects

        assertThat(replaceSubjects.subjects).isEqualTo(listOf(Subject("Maths")))
    }
}
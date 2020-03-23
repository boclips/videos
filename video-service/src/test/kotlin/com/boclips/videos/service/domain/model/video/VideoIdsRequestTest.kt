package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoIdsRequestTest {
    @Test
    fun `can convert to ids query when searching for everything`() {
        val id = TestFactories.createVideoId()
        val query = VideoIdsRequest(ids = listOf(id)).toSearchQuery(VideoAccess.Everything)

        assertThat(query.ids).containsExactlyInAnyOrder(id.value)
    }

    @Test
    fun `can convert to ids query with permitted videos`() {
        val id = TestFactories.createVideoId()
        val allowedVideos = setOf(TestFactories.createVideoId(), TestFactories.createVideoId())
        val query =
            VideoIdsRequest(ids = listOf(id)).toSearchQuery(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.IncludedIds(
                            allowedVideos
                        )
                    )
                )
            )

        assertThat(query.ids).containsExactlyInAnyOrder(id.value)
        assertThat(query.permittedVideoIds).containsExactlyInAnyOrder(*allowedVideos.map { it.value }.toTypedArray())
    }
}
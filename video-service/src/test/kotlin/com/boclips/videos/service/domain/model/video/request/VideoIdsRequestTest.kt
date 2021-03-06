package com.boclips.videos.service.domain.model.video.request

import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoIdsRequestTest {
    @Test
    fun `can convert to ids query when searching for everything`() {
        val id = TestFactories.createVideoId()
        val query = VideoIdsRequest(
            ids = listOf(
                id
            )
        ).toSearchQuery(VideoAccess.Everything(emptySet()))

        assertThat(query.userQuery.ids).containsExactlyInAnyOrder(id.value)
    }
}

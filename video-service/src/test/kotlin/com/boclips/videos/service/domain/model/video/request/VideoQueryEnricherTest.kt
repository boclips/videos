package com.boclips.videos.service.domain.model.video.request

import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VoiceType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import com.boclips.search.service.domain.videos.model.VoiceType as SearchVoiceType

class VideoQueryEnricherTest {
    @Test
    fun `can convert to ids query with permitted videos`() {
        val id = TestFactories.createVideoId()
        val allowedVideos = setOf(TestFactories.createVideoId(), TestFactories.createVideoId())
        val originalQuery = VideoQuery(ids = setOf(id.value))
        val query = VideoQueryEnricher.enrichFromAccessRules(
            originalQuery,
            videoAccess = VideoAccess.Rules(
                listOf(
                    VideoAccessRule.IncludedIds(allowedVideos)
                )
            )
        )

        Assertions.assertThat(query.ids).containsExactlyInAnyOrder(id.value)
        Assertions.assertThat(query.permittedVideoIds)
            .containsExactlyInAnyOrder(*allowedVideos.map { it.value }.toTypedArray())
    }

    @Test
    fun `can convert voice types to filter`() {
        val query = VideoQueryEnricher.enrichFromAccessRules(
            VideoQuery(phrase = "hello"),
            videoAccess = VideoAccess.Rules(
                listOf(
                    VideoAccessRule.IncludedVideoVoiceTypes(
                        voiceTypes = setOf(
                            VoiceType.WITH_VOICE,
                            VoiceType.UNKNOWN,
                            VoiceType.WITHOUT_VOICE
                        )
                    )
                )
            )
        )

        Assertions.assertThat(query.includedVoiceType).containsExactly(
            SearchVoiceType.WITH,
            SearchVoiceType.UNKNOWN,
            SearchVoiceType.WITHOUT
        )
    }
}

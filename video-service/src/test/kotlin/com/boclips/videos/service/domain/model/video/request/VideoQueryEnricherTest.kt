package com.boclips.videos.service.domain.model.video.request

import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VoiceType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
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

        assertThat(query.ids).containsExactlyInAnyOrder(id.value)
        assertThat(query.permittedVideoIds)
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

        assertThat(query.includedVoiceType).containsExactly(
            SearchVoiceType.WITH,
            SearchVoiceType.UNKNOWN,
            SearchVoiceType.WITHOUT
        )
    }

    @Test
    fun `respects previously set content type filters`() {
        val query = VideoQueryEnricher.enrichFromAccessRules(
            VideoQuery(
                phrase = "hello",
                includedTypes = setOf(VideoType.NEWS)
            ),
            videoAccess = VideoAccess.Rules(
                listOf(
                    VideoAccessRule.IncludedContentTypes(
                        contentTypes = setOf(ContentType.INSTRUCTIONAL_CLIPS)
                    )
                )
            )
        )

        assertThat(query.includedTypes).containsExactlyInAnyOrder(VideoType.INSTRUCTIONAL, VideoType.NEWS)
    }
}

package com.boclips.videos.service.domain.model.video.request

import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VoiceType
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import com.boclips.search.service.domain.videos.model.VoiceType as SearchVoiceType

class AccessRuleQueryConverterTest {
    @Test
    fun `can convert access rules to query`() {
        val allowedVideos = setOf(TestFactories.createVideoId(), TestFactories.createVideoId())
        val deniedVideoIds = setOf(TestFactories.createVideoId())
        val query = AccessRuleQueryConverter.fromAccessRules(
            videoAccess = VideoAccess.Rules(
                listOf(
                    VideoAccessRule.IncludedIds(allowedVideos),
                    VideoAccessRule.IncludedVideoVoiceTypes(
                        voiceTypes = setOf(
                            VoiceType.WITH_VOICE,
                            VoiceType.UNKNOWN,
                            VoiceType.WITHOUT_VOICE
                        )
                    ),
                    VideoAccessRule.IncludedContentTypes(
                        contentTypes = setOf(ContentType.INSTRUCTIONAL_CLIPS)
                    ),
                    VideoAccessRule.ExcludedContentTypes(contentTypes = setOf(ContentType.NEWS)),
                    VideoAccessRule.IncludedChannelIds(channelIds = setOf(ChannelId("hi"))),
                    VideoAccessRule.ExcludedIds(videoIds = deniedVideoIds),
                    VideoAccessRule.IncludedDistributionMethods(distributionMethods = setOf(DistributionMethod.DOWNLOAD)),
                    VideoAccessRule.ExcludedChannelIds(channelIds = setOf(ChannelId("HELLO")))

                )
            )
        )

        assertThat(query.permittedVideoIds)
            .containsExactlyInAnyOrder(*allowedVideos.map { it.value }.toTypedArray())
        assertThat(query.deniedVideoIds).containsExactly(*deniedVideoIds.map { it.value }.toTypedArray())
        assertThat(query.excludedTypes).containsExactly(VideoType.NEWS)
        assertThat(query.includedTypes).containsExactly(VideoType.INSTRUCTIONAL)
        assertThat(query.excludedContentPartnerIds).containsExactly("HELLO")
        assertThat(query.includedChannelIds).containsExactly("hi")
        assertThat(query.isEligibleForStream).isFalse()
        assertThat(query.includedVoiceType).containsExactly(
            SearchVoiceType.WITH,
            SearchVoiceType.UNKNOWN,
            SearchVoiceType.WITHOUT
        )
    }
}

package com.boclips.videos.service.domain.model.video.request

import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.search.service.domain.videos.model.VideoType as SearchVideoType
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VoiceType
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Locale
import com.boclips.search.service.domain.videos.model.VoiceType as SearchVoiceType

class VideoAccessRuleQueryConverterTest {
    @Test
    fun `can convert access rules to query`() {
        val allowedVideos = setOf(TestFactories.createVideoId(), TestFactories.createVideoId())
        val deniedVideoIds = setOf(TestFactories.createVideoId())
        val query = AccessRuleQueryConverter.toVideoAccessRuleQuery(
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
                        contentTypes = setOf(VideoType.INSTRUCTIONAL_CLIPS)
                    ),
                    VideoAccessRule.ExcludedContentTypes(contentTypes = setOf(VideoType.NEWS)),
                    VideoAccessRule.IncludedChannelIds(channelIds = setOf(ChannelId("hi"))),
                    VideoAccessRule.ExcludedIds(videoIds = deniedVideoIds),
                    VideoAccessRule.IncludedDistributionMethods(distributionMethods = setOf(DistributionMethod.DOWNLOAD)),
                    VideoAccessRule.ExcludedChannelIds(channelIds = setOf(ChannelId("HELLO"))),
                    VideoAccessRule.ExcludedLanguages(languages = setOf(Locale.FRENCH, Locale.KOREAN))
                )
            )
        )

        assertThat(query.permittedVideoIds)
            .containsExactlyInAnyOrder(*allowedVideos.map { it.value }.toTypedArray())
        assertThat(query.deniedVideoIds).containsExactly(*deniedVideoIds.map { it.value }.toTypedArray())
        assertThat(query.excludedTypes).containsExactly(SearchVideoType.NEWS)
        assertThat(query.includedTypes).containsExactly(SearchVideoType.INSTRUCTIONAL)
        assertThat(query.excludedContentPartnerIds).containsExactly("HELLO")
        assertThat(query.includedChannelIds).containsExactly("hi")
        assertThat(query.isEligibleForStream).isNull()
        assertThat(query.isEligibleForDownload).isTrue
        assertThat(query.includedVoiceType).containsExactly(
            SearchVoiceType.WITH,
            SearchVoiceType.UNKNOWN,
            SearchVoiceType.WITHOUT
        )
        assertThat(query.excludedLanguages).containsExactly(Locale.FRENCH, Locale.KOREAN)
    }
}

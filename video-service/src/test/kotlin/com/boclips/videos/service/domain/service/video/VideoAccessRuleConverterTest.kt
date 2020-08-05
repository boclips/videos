package com.boclips.videos.service.domain.service.video

import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VideoAccessRuleConverterTest {
    val converter = VideoAccessRuleConverter

    @Nested
    inner class ToPermittedIds {
        @Test
        fun `returns null when access to everything`() {
            val ids = converter.mapToPermittedVideoIds(VideoAccess.Everything)
            assertThat(ids).isNull()
        }

        @Test
        fun `returns null when no IncludedIds in rules`() {
            val ids = converter.mapToPermittedVideoIds(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.ExcludedIds(
                            videoIds = setOf(TestFactories.createVideoId())
                        )
                    )
                )
            )

            assertThat(ids).isNull()
        }

        @Test
        fun `returns permitted ids with correct rules`() {
            val videoId = TestFactories.createVideoId()
            val ids = converter.mapToPermittedVideoIds(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.IncludedIds(
                            videoIds = setOf(videoId)
                        )
                    )
                )
            )

            assertThat(ids).containsExactly(videoId.value)
        }
    }

    @Nested
    inner class ToDeniedVideoIds {
        @Test
        fun `returns null when access to everything`() {
            val ids = converter.mapToDeniedVideoIds(VideoAccess.Everything)
            assertThat(ids).isNull()
        }

        @Test
        fun `returns null when no ExcludedIds access rules`() {
            val ids = converter.mapToDeniedVideoIds(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.IncludedIds(
                            videoIds = setOf(TestFactories.createVideoId())
                        )
                    )
                )
            )

            assertThat(ids).isNull()
        }

        @Test
        fun `returns denied ids with correct rules`() {
            val videoId = TestFactories.createVideoId()
            val ids = converter.mapToDeniedVideoIds(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.ExcludedIds(
                            videoIds = setOf(videoId)
                        )
                    )
                )
            )

            assertThat(ids).containsExactly(videoId.value)
        }
    }

    @Nested
    inner class ToExcludedVideoTypes {
        @Test
        fun `returns empty when access to everything`() {
            val excludedTypes = converter.mapToExcludedVideoTypes(VideoAccess.Everything)
            assertThat(excludedTypes).isEmpty()
        }

        @Test
        fun `returns empty when no ExcludedVideoTypes in rules`() {
            val videoId = TestFactories.createVideoId()
            val excludedTypes = converter.mapToExcludedVideoTypes(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.ExcludedIds(
                            videoIds = setOf(videoId)
                        )
                    )
                )
            )
            assertThat(excludedTypes).isEmpty()
        }

        @Test
        fun `returns excluded video types if specified`() {
            val excludedTypes = converter.mapToExcludedVideoTypes(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.ExcludedContentTypes(
                            contentTypes = setOf(ContentType.STOCK)
                        )
                    )
                )
            )
            assertThat(excludedTypes).containsOnly(VideoType.STOCK)
        }
    }

    @Nested
    inner class ToIncludedVideoTypes {
        @Test
        fun `returns empty when access to everything`() {
            val excludedTypes = converter.mapToIncludedVideoTypes(VideoAccess.Everything)
            assertThat(excludedTypes).isEmpty()
        }

        @Test
        fun `returns empty when no included video type in rules`() {
            val videoId = TestFactories.createVideoId()
            val excludedTypes = converter.mapToIncludedVideoTypes(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.ExcludedIds(
                            videoIds = setOf(videoId)
                        )
                    )
                )
            )
            assertThat(excludedTypes).isEmpty()
        }

        @Test
        fun `returns included video types if specified`() {
            val includedTypes = converter.mapToIncludedVideoTypes(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.IncludedContentTypes(
                            contentTypes = setOf(ContentType.STOCK)
                        )
                    )
                )
            )
            assertThat(includedTypes).containsOnly(VideoType.STOCK)
        }
    }

    @Nested
    inner class ToExcludedContentPartnersIds {
        @Test
        fun `returns empty when access to everything`() {
            val excludedIds = converter.mapToExcludedChannelIds(VideoAccess.Everything)
            assertThat(excludedIds).isEmpty()
        }

        @Test
        fun `returns empty when no excluded channel ids in rules`() {
            val videoId = TestFactories.createVideoId()
            val excludedIds = converter.mapToExcludedChannelIds(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.ExcludedIds(
                            videoIds = setOf(videoId)
                        )
                    )
                )
            )
            assertThat(excludedIds).isEmpty()
        }

        @Test
        fun `returns excluded channels if specified`() {
            val excludedIds = converter.mapToExcludedChannelIds(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.ExcludedChannelIds(
                            channelIds = setOf(
                                ChannelId(
                                    value = "123"
                                )
                            )
                        )
                    )
                )
            )
            assertThat(excludedIds).containsOnly("123")
        }
    }

    @Nested
    inner class ToIncludedChannelIds {
        @Test
        fun `returns empty when access to everything`() {
            val includedChannelIds = converter.mapToIncludedChannelIds(VideoAccess.Everything)
            assertThat(includedChannelIds).isEmpty()
        }

        @Test
        fun `returns empty when no included channel ids in rules`() {
            val videoId = TestFactories.createVideoId()
            val includedChannelIds = converter.mapToIncludedChannelIds(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.ExcludedIds(
                            videoIds = setOf(videoId)
                        )
                    )
                )
            )
            assertThat(includedChannelIds).isEmpty()
        }

        @Test
        fun `returns included channels if specified`() {
            val includedChannelIds = converter.mapToIncludedChannelIds(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.IncludedChannelIds(
                            channelIds = setOf(
                                ChannelId(
                                    value = "123"
                                )
                            )
                        )
                    )
                )
            )
            assertThat(includedChannelIds).containsOnly("123")
        }
    }

    @Nested
    inner class ToIsEligibleForStreaming() {
        @Test
        fun `returns nothing when access to everything`() {
            val isEligibleForStreaming = converter.isEligibleForStreaming(VideoAccess.Everything)
            assertThat(isEligibleForStreaming).isNull()
        }

        @Test
        fun `returns true with streaming access rule`() {
            val isEligibleForStreaming = converter.isEligibleForStreaming(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.IncludedDistributionMethods(
                            setOf(DistributionMethod.STREAM)
                        )
                    )
                )
            )
            assertThat(isEligibleForStreaming).isTrue()
        }

        @Test
        fun `returns false with no stream access rule`() {
            val isEligibleForStreaming = converter.isEligibleForStreaming(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.IncludedDistributionMethods(
                            setOf(DistributionMethod.DOWNLOAD)
                        )
                    )
                )
            )
            assertThat(isEligibleForStreaming).isFalse()
        }

        @Test
        fun `returns nothing when streaming rule is not specified`() {
            val isEligibleForStreaming = converter.isEligibleForStreaming(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.IncludedIds(
                            setOf(TestFactories.createVideoId())
                        )
                    )
                )
            )
            assertThat(isEligibleForStreaming).isNull()
        }
    }
}

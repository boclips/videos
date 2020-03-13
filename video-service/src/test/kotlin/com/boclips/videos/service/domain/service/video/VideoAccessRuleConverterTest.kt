package com.boclips.videos.service.domain.service.video

import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.domain.model.video.ContentPartnerId
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
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
    inner class ToExcludedContentPartnersIds {
        @Test
        fun `returns empty when access to everything`() {
            val excludedIds = converter.mapToExcludedContentPartnerIds(VideoAccess.Everything)
            assertThat(excludedIds).isEmpty()
        }

        @Test
        fun `returns empty when no ExcludedVideoTypes in rules`() {
            val videoId = TestFactories.createVideoId()
            val excludedIds = converter.mapToExcludedContentPartnerIds(
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
        fun `returns excluded video types if specified`() {
            val excludedIds = converter.mapToExcludedContentPartnerIds(
                VideoAccess.Rules(
                    listOf(
                        VideoAccessRule.ExcludedContentPartners(
                            contentPartnerIds = setOf(ContentPartnerId(value = "123"))
                        )
                    )
                )
            )
            assertThat(excludedIds).containsOnly("123")
        }
    }
}
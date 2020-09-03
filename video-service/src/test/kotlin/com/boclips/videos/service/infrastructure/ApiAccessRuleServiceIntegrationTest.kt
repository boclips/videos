package com.boclips.videos.service.infrastructure

import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.users.api.factories.AccessRulesResourceFactory
import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.api.request.collection.CreateCollectionRequest
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VoiceType
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ApiAccessRuleServiceIntegrationTest : AbstractSpringIntegrationTest() {
    fun createAccessRulesResource(userId: String, rules: List<AccessRuleResource>) {
        usersClient.addAccessRules(
            userId,
            AccessRulesResourceFactory.sample(*rules.toTypedArray())
        )
    }

    @Test
    fun `passes through client's response if all is well`() {
        val videoId = ObjectId().toHexString()
        val collectionId = createCollection(
            CreateCollectionRequest(title = "hello there", videos = listOf(videoId)),
            UserFactory.sample(id = "another-user")
        ).id

        createAccessRulesResource(
            "test-user", listOf(
                AccessRuleResource.IncludedCollections(
                    id = "access-rule-id",
                    name = "Test Contract",
                    collectionIds = listOf(collectionId.value)
                ),
                AccessRuleResource.IncludedVideoTypes(
                    id = "access-rule-id",
                    name = "Test Contract",
                    videoTypes = listOf("NEWS")
                )
            )
        )

        val accessRules = accessRuleService.getRules(UserFactory.sample(id = "test-user"))

        assertThat(accessRules.collectionAccess).isEqualTo(
            CollectionAccessRule.SpecificIds(setOf(collectionId))
        )
        assertThat((accessRules.videoAccess as VideoAccess.Rules).accessRules).containsExactly(
            VideoAccessRule.IncludedIds(
                videoIds = setOf(VideoId(videoId))
            ),
            VideoAccessRule.IncludedContentTypes(contentTypes = setOf(ContentType.NEWS))
        )
    }

    @Test
    fun `returns default when content package cannot be found`() {
        val accessRules = accessRuleService.getRules(UserFactory.sample(id = "test-user"))

        assertThat(accessRules.collectionAccess).isEqualTo(CollectionAccessRule.everything())

        assertThat(accessRules.videoAccess).isEqualTo(VideoAccess.Everything)
    }

    @Nested
    inner class CollectionsAccess {
        @Test
        fun `has access specific all collections when no contracts specified`() {
            createAccessRulesResource("test-user", emptyList())

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            assertThat(accessRules.collectionAccess).isEqualTo(CollectionAccessRule.Everything)
        }

        @Test
        fun `has access to everything if is allowed to view any collection`() {
            createAccessRulesResource("test-user", emptyList())

            val user = UserFactory.sample(id = "test-user", isPermittedToViewAnyCollection = true)
            val accessRules = accessRuleService.getRules(user)

            assertThat(accessRules.collectionAccess).isEqualTo(CollectionAccessRule.Everything)
        }

        @Test
        fun `access to collection provides access to collection's videos`() {
            val firstId = TestFactories.createVideoId()
            val user = UserFactory.sample(id = "test-user")
            val collectionId = createCollection(
                CreateCollectionRequest(
                    title = "hello there",
                    videos = listOf(firstId.value)
                ), user
            ).id.value

            createAccessRulesResource(
                "test-user",
                listOf(
                    AccessRuleResource.IncludedCollections(
                        id = "access-rule-id",
                        name = "bad videos",
                        collectionIds = listOf(collectionId)
                    )
                )
            )

            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsOnly(VideoAccessRule.IncludedIds(setOf(firstId)))
        }
    }

    @Nested
    inner class AccessingVideos {
        @Test
        fun `has access to everything when no contracts specified`() {
            createAccessRulesResource("test-user", emptyList())

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            assertThat(accessRules.videoAccess).isEqualTo(VideoAccess.Everything)
        }

        @Test
        fun `can convert ExcludedVideosAccess rule to domain`() {
            val firstId = TestFactories.createVideoId()
            val secondId = TestFactories.createVideoId()

            createAccessRulesResource(
                "test-user",
                listOf(
                    AccessRuleResource.ExcludedVideos(
                        id = "access-rule-id",
                        name = "bad videos",
                        videoIds = listOf(firstId.value, secondId.value)
                    )
                )
            )

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsOnly(VideoAccessRule.ExcludedIds(setOf(firstId, secondId)))
        }

        @Test
        fun `can convert ExcludedVideoTypesAccess rule to domain`() {
            createAccessRulesResource(
                "test-user", listOf(
                    AccessRuleResource.ExcludedVideoTypes(
                        id = "access-rule-id",
                        name = "bad video types",
                        videoTypes = listOf("NEWS")
                    )
                )
            )
            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsOnly(VideoAccessRule.ExcludedContentTypes(setOf(ContentType.NEWS)))
        }

        @Test
        fun `can convert IncludedVideoTypesAccess rule to domain`() {
            createAccessRulesResource(
                "test-user", listOf(
                    AccessRuleResource.IncludedVideoTypes(
                        id = "access-rule-id",
                        name = "bad video types",
                        videoTypes = listOf("NEWS", "STOCK", "INSTRUCTIONAL")
                    )
                )
            )
            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsOnly(
                VideoAccessRule.IncludedContentTypes(
                    setOf(
                        ContentType.NEWS,
                        ContentType.INSTRUCTIONAL_CLIPS,
                        ContentType.STOCK
                    )
                )
            )
        }

        @Test
        fun `ignores any unknown content types`() {
            createAccessRulesResource(
                "test-user",
                listOf(
                    AccessRuleResource.ExcludedVideoTypes(
                        id = "access-rule-id",
                        name = "bad video types",
                        videoTypes = listOf("UNKNOWN", "NEWS")
                    )
                )
            )

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsExactly(VideoAccessRule.ExcludedContentTypes(setOf(ContentType.NEWS)))
        }

        @Test
        fun `access to everything if only unknown content types are specified`() {
            createAccessRulesResource(
                "test-user",
                listOf(
                    AccessRuleResource.ExcludedVideoTypes(
                        id = "access-rule-id",
                        name = "bad video types",
                        videoTypes = listOf("UNKNOWN")
                    )
                )
            )

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            assertThat(accessRules.videoAccess is VideoAccess.Everything).isTrue()
        }

        @Test
        fun `can convert ExcludedContent types to domain`() {
            createAccessRulesResource(
                "test-user",
                listOf(
                    AccessRuleResource.ExcludedChannels(
                        id = "access-rule-id",
                        name = "bad video types",
                        channelIds = listOf("content-partner-1")
                    )
                )
            )

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsOnly(
                VideoAccessRule.ExcludedChannelIds(
                    channelIds = setOf(
                        ChannelId(
                            value = "content-partner-1"
                        )
                    )
                )
            )
        }

        @Test
        fun `can convert IncludedDistributionMethod types to domain`() {
            createAccessRulesResource(
                "test-user",
                listOf(
                    AccessRuleResource.IncludedDistributionMethods(
                        id = "access-rule-id",
                        name = "bad video types",
                        distributionMethods = listOf("STREAM", "DOWNLOAD")
                    )
                )
            )
            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsOnly(
                VideoAccessRule.IncludedDistributionMethods(
                    distributionMethods = setOf(
                        DistributionMethod.STREAM,
                        DistributionMethod.DOWNLOAD
                    )
                )
            )
        }

        @Test
        fun `can convert all video access rule types to domain`() {
            val firstId = TestFactories.createVideoId()
            val secondId = TestFactories.createVideoId()
            createAccessRulesResource(
                "test-user", listOf(
                    AccessRuleResource.IncludedVideos(
                        id = "access-rule-id",
                        name = "good videos",
                        videoIds = listOf(firstId.value)
                    ),
                    AccessRuleResource.ExcludedVideos(
                        id = "access-rule-id2",
                        name = "bad videos",
                        videoIds = listOf(secondId.value)
                    )
                )
            )

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules

            assertThat(videoAccess.accessRules).containsExactlyInAnyOrder(
                VideoAccessRule.IncludedIds(setOf(firstId)),
                VideoAccessRule.ExcludedIds(setOf(secondId))
            )
        }

        @Test
        fun `can convert included channel rule to domain`() {
            createAccessRulesResource(
                "test-user",
                listOf(
                    AccessRuleResource.IncludedChannels(
                        id = "access-rule-id",
                        name = "good channels",
                        channelIds = listOf("123")
                    )
                )
            )

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val channelAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(channelAccess.accessRules).containsExactlyInAnyOrder(
                VideoAccessRule.IncludedChannelIds(
                    setOf(
                        ChannelId("123")
                    )
                )
            )
        }

        @Test
        fun `can convert included video voice type to domain`() {
            createAccessRulesResource(
                "test-user",
                listOf(
                    AccessRuleResource.IncludedVideoVoiceTypes(
                        id = "access-rule-id",
                        name = "voices",
                        voiceTypes = listOf("bad", "WITH_VOICE", "WITHOUT_VOICE", "UNKNOWN_VOICE")
                    )
                )
            )

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsExactly(
                VideoAccessRule.IncludedVideoVoiceTypes(
                    setOf(
                        VoiceType.WITH_VOICE,
                        VoiceType.WITHOUT_VOICE,
                        VoiceType.UNKNOWN
                    )
                )
            )
        }
    }
}

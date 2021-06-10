package com.boclips.videos.service.infrastructure

import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.users.api.factories.AccessRuleResourceFactory
import com.boclips.users.api.factories.AccessRulesResourceFactory
import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.api.request.collection.CreateCollectionRequest
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.*
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

class ApiAccessRuleServiceIntegrationTest : AbstractSpringIntegrationTest() {
    fun createAccessRulesResource(userId: String, rules: List<AccessRuleResource>, client: String? = null) {
        usersClient.addAccessRules(
            userId,
            AccessRulesResourceFactory.sample(*rules.toTypedArray()),
            client
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
                    name = "Test Contract",
                    collectionIds = listOf(collectionId.value)
                ),
                AccessRuleResource.IncludedVideoTypes(
                    name = "Test Contract",
                    videoTypes = listOf("NEWS")
                ),
                AccessRuleResource.ExcludedPlaybackSources(
                    name = "test",
                    sources = setOf("YOUTUBE")
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
            VideoAccessRule.IncludedContentTypes(contentTypes = setOf(VideoType.NEWS)),
            VideoAccessRule.ExcludedPlaybackProviderTypes(sources = setOf(PlaybackProviderType.YOUTUBE))
        )
    }

    @Test
    fun `returns default when content package cannot be found`() {
        val accessRules = accessRuleService.getRules(UserFactory.sample(id = "test-user"))

        assertThat(accessRules.collectionAccess).isEqualTo(CollectionAccessRule.everything())

        assertThat(accessRules.videoAccess).isEqualTo(VideoAccess.Everything(emptySet()))
    }

    @Test
    fun `default includes private channels`() {
        val privateChannel = saveChannel(private = true)
        val accessRules = accessRuleService.getRules(UserFactory.sample(id = "test-user"))

        assertThat(accessRules.collectionAccess).isEqualTo(CollectionAccessRule.everything())

        assertThat(accessRules.videoAccess).isEqualTo(VideoAccess.Everything(setOf(ChannelId(privateChannel.id.value))))
    }

    @Test
    fun `returns client based access rules`() {
        val hqVideoId = ObjectId().toHexString()
        val teachersVideoId = ObjectId().toHexString()
        createAccessRulesResource(
            "test-user",
            listOf(AccessRuleResourceFactory.sampleExcludedVideos(name = "video rule", videoIds = listOf(hqVideoId))),
            "hq"
        )

        createAccessRulesResource(
            "test-user",
            listOf(AccessRuleResourceFactory.sampleExcludedVideos(name = "rule 2", videoIds = listOf(teachersVideoId))),
            "teachers"
        )

        val accessRules = accessRuleService.getRules(UserFactory.sample(id = "test-user"), "hq")

        val videoAccessRules = accessRules.videoAccess as? VideoAccess.Rules
        val firstRule = videoAccessRules?.accessRules?.first() as? VideoAccessRule.ExcludedIds
        assertThat(firstRule?.videoIds?.first()).isEqualTo(VideoId(hqVideoId))
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

            assertThat(accessRules.videoAccess).isEqualTo(VideoAccess.Everything(emptySet()))
        }

        @Test
        fun `can convert ExcludedVideosAccess rule to domain`() {
            val firstId = TestFactories.createVideoId()
            val secondId = TestFactories.createVideoId()

            createAccessRulesResource(
                "test-user",
                listOf(
                    AccessRuleResource.ExcludedVideos(
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
                        name = "bad video types",
                        videoTypes = listOf("NEWS")
                    )
                )
            )
            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsOnly(VideoAccessRule.ExcludedContentTypes(setOf(VideoType.NEWS)))
        }

        @Test
        fun `can convert IncludedVideoTypesAccess rule to domain`() {
            createAccessRulesResource(
                "test-user", listOf(
                    AccessRuleResource.IncludedVideoTypes(
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
                        VideoType.NEWS,
                        VideoType.INSTRUCTIONAL_CLIPS,
                        VideoType.STOCK
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
                        name = "bad video types",
                        videoTypes = listOf("UNKNOWN", "NEWS")
                    )
                )
            )

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsExactly(VideoAccessRule.ExcludedContentTypes(setOf(VideoType.NEWS)))
        }

        @Test
        fun `access to everything if only unknown content types are specified`() {
            createAccessRulesResource(
                "test-user",
                listOf(
                    AccessRuleResource.ExcludedVideoTypes(
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
                        name = "good videos",
                        videoIds = listOf(firstId.value)
                    ),
                    AccessRuleResource.ExcludedVideos(
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

        @Test
        fun `can convert excluded languages to domain`() {
            createAccessRulesResource(
                "test-user",
                listOf(
                    AccessRuleResource.ExcludedLanguages(
                        name = "voices",
                        languages = setOf(Locale.ENGLISH.toLanguageTag(), Locale.FRENCH.toLanguageTag())
                    )
                )
            )

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsExactly(
                VideoAccessRule.ExcludedLanguages(
                    setOf(
                        Locale.ENGLISH,
                        Locale.FRENCH
                    )
                )
            )
        }

        @Test
        fun `can convert excluded playback sources to domain`() {
            createAccessRulesResource(
                "test-user",
                listOf(
                    AccessRuleResource.ExcludedPlaybackSources(
                        name = "sources",
                        sources = setOf("YOUTUBE", "KALTURA")
                    )
                )
            )

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsExactly(
                VideoAccessRule.ExcludedPlaybackProviderTypes(
                    setOf(PlaybackProviderType.YOUTUBE, PlaybackProviderType.KALTURA)
                )
            )
        }
    }
}

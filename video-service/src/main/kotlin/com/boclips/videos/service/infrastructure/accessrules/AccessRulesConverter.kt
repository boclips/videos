package com.boclips.videos.service.infrastructure.accessrules

import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.service.application.accessrules.AccessRulesConverter
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.*
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.infrastructure.user.ApiAccessRuleService
import java.util.*

class ApiAccessRulesConverter(
    private val collectionRepository: CollectionRepository
) : AccessRulesConverter {
    override fun toVideoAccess(
        accessRules: List<AccessRuleResource>
    ): VideoAccess {
        val videoAccessRules = accessRules.mapNotNull {
            when (it) {
                is AccessRuleResource.IncludedDistributionMethods -> VideoAccessRule.IncludedDistributionMethods(
                    it.distributionMethods.map { method -> DistributionMethod.valueOf(method) }.toSet()
                )
                is AccessRuleResource.IncludedVideos -> VideoAccessRule.IncludedIds(
                    it.videoIds.map { id -> VideoId(id) }.toSet()
                )
                is AccessRuleResource.ExcludedVideos -> VideoAccessRule.ExcludedIds(
                    it.videoIds.map { id -> VideoId(id) }.toSet()
                )
                is AccessRuleResource.ExcludedVideoTypes -> extractExcludedContentTypes(it)
                is AccessRuleResource.IncludedVideoTypes -> extractIncludedContentTypes(it)
                is AccessRuleResource.ExcludedChannels -> VideoAccessRule.ExcludedChannelIds(
                    it.channelIds.map { id ->
                        ChannelId(
                            id
                        )
                    }.toSet()
                )
                is AccessRuleResource.IncludedChannels -> VideoAccessRule.IncludedChannelIds(
                    it.channelIds.map { id ->
                        ChannelId(
                            id
                        )
                    }.toSet()
                )
                is AccessRuleResource.IncludedCollections -> extractIncludedVideoIds(it)
                is AccessRuleResource.IncludedVideoVoiceTypes -> VideoAccessRule.IncludedVideoVoiceTypes(
                    it.voiceTypes.mapNotNull { voiceTypes ->
                        when (voiceTypes) {
                            "WITH_VOICE" -> VoiceType.WITH_VOICE
                            "WITHOUT_VOICE" -> VoiceType.WITHOUT_VOICE
                            "UNKNOWN_VOICE" -> VoiceType.UNKNOWN
                            else -> {
                                ApiAccessRuleService.logger.warn { "Invalid voice type: $voiceTypes" }
                                null
                            }
                        }
                    }.toSet()
                )
                is AccessRuleResource.ExcludedLanguages -> VideoAccessRule.ExcludedLanguages(
                    it.languages.map { languageTag -> Locale.forLanguageTag(languageTag) }.toSet()
                )
                is AccessRuleResource.ExcludedPlaybackSources -> VideoAccessRule.ExcludedPlaybackProviderTypes(
                    it.sources.mapNotNull { source ->
                        when (source) {
                            "KALTURA" -> PlaybackProviderType.KALTURA
                            "YOUTUBE" -> PlaybackProviderType.YOUTUBE
                            else -> {
                                ApiAccessRuleService.logger.warn { "Invalid playback source type: $source" }
                                null
                            }
                        }
                    }.toSet()
                )
            }
        }

        return when {
            videoAccessRules.isNotEmpty() -> VideoAccess.Rules(videoAccessRules)
            else -> VideoAccess.Everything
        }
    }

    override fun toCollectionAccess(
        accessRules: List<AccessRuleResource>,
        user: User?
    ): CollectionAccessRule {
        val collectionIds: List<CollectionId> = accessRules
            .flatMap { accessRule ->
                when (accessRule) {
                    is AccessRuleResource.IncludedCollections ->
                        accessRule.collectionIds.map { CollectionId(it) }
                    else -> emptyList()
                }
            }
        val isSuperUser = user?.isPermittedToModifyAnyCollection ?: false

        return when {
            collectionIds.isNotEmpty() && !isSuperUser ->
                CollectionAccessRule.specificIds(collectionIds)
            else -> CollectionAccessRule.everything()
        }
    }

    private fun extractExcludedContentTypes(
        accessRule: AccessRuleResource.ExcludedVideoTypes
    ): VideoAccessRule.ExcludedContentTypes? =
        convertVideoTypes(accessRule.videoTypes)
            .takeIf { contentTypes -> contentTypes.isNotEmpty() }
            ?.let { contentTypes ->
                VideoAccessRule.ExcludedContentTypes(
                    contentTypes = contentTypes.toSet()
                )
            }

    private fun extractIncludedContentTypes(
        accessRule: AccessRuleResource.IncludedVideoTypes
    ): VideoAccessRule.IncludedContentTypes? =
        convertVideoTypes(accessRule.videoTypes)
            .takeIf { contentTypes -> contentTypes.isNotEmpty() }
            ?.let { contentTypes ->
                VideoAccessRule.IncludedContentTypes(
                    contentTypes = contentTypes.toSet()
                )
            }

    private fun extractIncludedVideoIds(
        accessRule: AccessRuleResource.IncludedCollections
    ): VideoAccessRule {
        val videoIds = accessRule.collectionIds.flatMap {
            val collection = collectionRepository.find(CollectionId(it))
            collection?.videos ?: emptySet()

        }
        return VideoAccessRule.IncludedIds(videoIds.toSet())
    }

    private fun convertVideoTypes(videoTypes: List<String>): List<VideoType> {
        return videoTypes
            .mapNotNull { type ->
                when (type) {
                    "NEWS" -> VideoType.NEWS
                    "INSTRUCTIONAL" -> VideoType.INSTRUCTIONAL_CLIPS
                    "STOCK" -> VideoType.STOCK
                    else -> {
                        ApiAccessRuleService.logger.info { "Invalid Content Type: $type" }
                        null
                    }
                }
            }
    }
}

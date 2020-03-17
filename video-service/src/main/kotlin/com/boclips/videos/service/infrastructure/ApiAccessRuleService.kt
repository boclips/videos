package com.boclips.videos.service.infrastructure

import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.boclips.users.client.UserServiceClient
import com.boclips.users.client.model.accessrule.AccessRule
import com.boclips.users.client.model.accessrule.ExcludedContentPartnersAccessRule
import com.boclips.users.client.model.accessrule.ExcludedVideoTypesAccessRule
import com.boclips.users.client.model.accessrule.ExcludedVideosAccessRule
import com.boclips.users.client.model.accessrule.IncludedCollectionsAccessRule
import com.boclips.users.client.model.accessrule.IncludedDistributionMethodsAccessRule
import com.boclips.users.client.model.accessrule.IncludedVideosAccessRule
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.video.ContentPartnerId
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.AccessRuleService
import mu.KLogging
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable

open class ApiAccessRuleService(private val userServiceClient: UserServiceClient) : AccessRuleService {
    companion object : KLogging()

    @Retryable(
        maxAttempts = 3,
        backoff = Backoff(multiplier = 1.5)
    )
    override fun getRules(user: User): AccessRules {
        val retrievedAccessRules = userServiceClient.getAccessRules(user.id.value) ?: emptyList()

        val accessRules = retrievedAccessRules.let {
            AccessRules(
                collectionAccess = getCollectionAccessRule(it, user),
                videoAccess = getVideoAccessRule(it)
            )
        }
        logger.info { "User $user got rules $accessRules" }
        return accessRules
    }

    private fun getCollectionAccessRule(accessRules: List<AccessRule>, user: User): CollectionAccessRule {
        val collectionIds: List<CollectionId> = accessRules
            .flatMap { accessRule ->
                when (accessRule) {
                    is IncludedCollectionsAccessRule -> accessRule.collectionIds.map { CollectionId(it) }
                    else -> emptyList()
                }
            }

        return when {
            collectionIds.isNotEmpty() -> CollectionAccessRule.specificIds(
                collectionIds
            )
            user.isPermittedToViewAnyCollection -> CollectionAccessRule.everything()
            else -> CollectionAccessRule.asOwner(user.id)
        }
    }

    private fun getVideoAccessRule(accessRules: List<AccessRule>): VideoAccess {
        val videoAccessRules = accessRules.mapNotNull {
            when (it) {
                is IncludedVideosAccessRule -> VideoAccessRule.IncludedIds(it.videoIds.map { id -> VideoId(id) }
                    .toSet())
                is ExcludedVideosAccessRule -> VideoAccessRule.ExcludedIds(it.videoIds.map { id -> VideoId(id) }
                    .toSet())
                is ExcludedVideoTypesAccessRule -> extractExcludedContentTypes(it)
                is ExcludedContentPartnersAccessRule -> VideoAccessRule.ExcludedContentPartners(
                    it.contentPartnerIds.map { id -> ContentPartnerId(id) }.toSet()
                )
                is IncludedDistributionMethodsAccessRule -> VideoAccessRule.IncludedDistributionMethods(
                    distributionMethods = it.distributionMethods.map { method -> DistributionMethod.valueOf(method) }.toSet()
                )
                else -> null
            }
        }

        return when {
            videoAccessRules.isNotEmpty() -> VideoAccess.Rules(videoAccessRules)
            else -> VideoAccess.Everything
        }
    }

    private fun extractExcludedContentTypes(accessRule: ExcludedVideoTypesAccessRule): VideoAccessRule.ExcludedContentTypes? =
        accessRule.videoTypes
            .mapNotNull { type ->
                when (type) {
                    "NEWS" -> ContentType.NEWS
                    "INSTRUCTIONAL" -> ContentType.INSTRUCTIONAL_CLIPS
                    "STOCK" -> ContentType.STOCK
                    else -> {
                        logger.info { "Invalid Content Type: $type" }
                        null
                    }
                }
            }
            .takeIf { contentTypes -> contentTypes.isNotEmpty() }
            ?.let { contentTypes -> VideoAccessRule.ExcludedContentTypes(contentTypes = contentTypes.toSet()) }

    @Recover
    fun getRulesRecoveryMethod(e: Exception): AccessRules {
        logger.warn { "Unable to retrieve access rules, defaulting to access to public collections. Cause: $e" }
        return AccessRules(
            CollectionAccessRule.public(),
            VideoAccess.Everything
        )
    }
}

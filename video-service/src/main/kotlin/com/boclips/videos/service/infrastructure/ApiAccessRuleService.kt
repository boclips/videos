package com.boclips.videos.service.infrastructure

import com.boclips.users.client.UserServiceClient
import com.boclips.users.client.model.accessrule.AccessRule
import com.boclips.users.client.model.accessrule.ExcludedVideosAccessRule
import com.boclips.users.client.model.accessrule.IncludedCollectionsAccessRule
import com.boclips.users.client.model.accessrule.IncludedVideosAccessRule
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
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
        val retrievedAccessRules = userServiceClient.getContentPackage(user.id.value)?.accessRules ?: emptyList()

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
                is IncludedVideosAccessRule -> VideoAccessRule.IncludedIds(it.videoIds.map { id -> VideoId(id) }.toSet())
                is ExcludedVideosAccessRule -> VideoAccessRule.ExcludedIds(it.videoIds.map { id -> VideoId(id) }.toSet())
                else -> null
            }
        }

        return when {
            videoAccessRules.isNotEmpty() -> VideoAccess.Rules(videoAccessRules)
            else -> VideoAccess.Everything
        }
    }

    @Recover
    fun getRulesRecoveryMethod(e: Exception): AccessRules {
        logger.warn { "Unable to retrieve access rules, defaulting to access to public collections. Cause: $e" }
        return AccessRules(
            CollectionAccessRule.public(),
            VideoAccess.Everything
        )
    }
}

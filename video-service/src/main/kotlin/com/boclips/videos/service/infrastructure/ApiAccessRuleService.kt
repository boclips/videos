package com.boclips.videos.service.infrastructure

import com.boclips.users.client.UserServiceClient
import com.boclips.users.client.model.accessrule.AccessRule
import com.boclips.users.client.model.accessrule.SelectedCollectionsAccessRule
import com.boclips.users.client.model.accessrule.SelectedVideosAccessRule
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
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
        val accessRules = userServiceClient.getAccessRules(user.id.value)
        return AccessRules(
            collectionAccess = getCollectionAccessRule(accessRules, user),
            videoAccess = getVideoAccessRule(accessRules)
        )
    }

    private fun getCollectionAccessRule(accessRules: List<AccessRule>, user: User): CollectionAccessRule {
        val collectionIds: List<CollectionId> = accessRules
            .flatMap { accessRule ->
                when (accessRule) {
                    is SelectedCollectionsAccessRule -> accessRule.collectionIds.map { CollectionId(it) }
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

    private fun getVideoAccessRule(accessRules: List<AccessRule>): VideoAccessRule {
        val videoIds: List<VideoId> = accessRules.filterIsInstance<SelectedVideosAccessRule>()
            .flatMap { accessRule -> accessRule.videoIds.map { id -> VideoId(id) } }

        return when {
            videoIds.isNotEmpty() -> VideoAccessRule.SpecificIds(
                videoIds.toSet()
            )
            else -> VideoAccessRule.Everything
        }
    }

    @Recover
    fun getRulesRecoveryMethod(e: Exception): AccessRules {
        logger.warn { "Unable to retrieve access rules, defaulting to access to public collections. Cause: $e" }
        return AccessRules(
            CollectionAccessRule.public(),
            VideoAccessRule.Everything
        )
    }
}

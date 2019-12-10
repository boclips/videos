package com.boclips.videos.service.infrastructure

import com.boclips.security.utils.User
import com.boclips.users.client.UserServiceClient
import com.boclips.users.client.model.contract.SelectedCollectionsContract
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.service.AccessRule
import com.boclips.videos.service.domain.service.AccessRuleService
import com.boclips.videos.service.domain.service.CollectionAccessRule
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
    override fun getRules(user: User): AccessRule {
        val collectionIds: List<CollectionId> = userServiceClient.getContracts(user.id)
            .flatMap { contract ->
                when (contract) {
                    is SelectedCollectionsContract -> contract.collectionIds.map { CollectionId(it) }
                    else -> emptyList()
                }
            }
        return AccessRule(
            when {
                collectionIds.isNotEmpty() -> CollectionAccessRule.specificIds(collectionIds)
                user.hasRole(UserRoles.VIEW_ANY_COLLECTION) -> CollectionAccessRule.everything()
                else -> CollectionAccessRule.asOwner(UserId(user.id))
            }
        )
    }

    @Recover
    fun getRulesRecoveryMethod(e: Exception): AccessRule {
        logger.warn { "Unable to retrieve access rules, defaulting to access to public collections. Cause: $e" }
        return AccessRule(CollectionAccessRule.public())
    }
}

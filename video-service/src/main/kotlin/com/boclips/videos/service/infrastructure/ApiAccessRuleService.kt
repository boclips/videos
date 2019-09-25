package com.boclips.videos.service.infrastructure

import com.boclips.users.client.UserServiceClient
import com.boclips.users.client.model.contract.SelectedContentContract
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.AccessRule
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
    override fun getRules(userId: String): AccessRule {
        val collectionIds: List<CollectionId> = userServiceClient.getContracts(userId)
            .flatMap { contract ->
                when (contract) {
                    is SelectedContentContract -> contract.collectionIds.map { CollectionId(it) }
                    else -> emptyList()
                }
            }
        return AccessRule.build(collectionIds)
    }

    @Recover
    fun getContractsRecoveryMethod(e: Exception): AccessRule {
        logger.warn { "Unable to retrieve access rules, defaulting to access to everything. Cause: $e" }
        return AccessRule.build(emptyList())
    }
}

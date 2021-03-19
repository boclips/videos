package com.boclips.videos.service.infrastructure.user

import com.boclips.users.api.httpclient.UsersClient
import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.service.application.accessrules.AccessRulesConverter
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.service.user.AccessRuleService
import feign.FeignException
import mu.KLogging
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable

open class ApiAccessRuleService(
    private val usersClient: UsersClient,
    private val accessRulesConverter: AccessRulesConverter
) :
    AccessRuleService {
    companion object : KLogging()

    @Retryable(
        maxAttempts = 3,
        backoff = Backoff(multiplier = 1.5)
    )
    override fun getRules(user: User, client: String?): AccessRules {
        user.id ?: return AccessRules.anonymousAccess()

        val retrievedAccessRules: List<AccessRuleResource> =
            try {
                usersClient.getAccessRulesOfUser(user.id.value, client)._embedded.accessRules
            } catch (ex: FeignException) {
                when (ex.status()) {
                    404 -> emptyList()
                    else -> throw ex
                }
            }

        val accessRules = retrievedAccessRules.let {
            AccessRules(
                collectionAccess = accessRulesConverter.toCollectionAccess(it, user),
                videoAccess = accessRulesConverter.toVideoAccess(it)
            )
        }

        logger.info { "Retrieved access rules for user ${user.id.value}" }
        return accessRules
    }

    @Recover
    fun getRulesRecoveryMethod(e: Exception): AccessRules {
        logger.warn {
            "Unable to retrieve access rules, defaulting to access to public " +
                "collections. Cause: $e"
        }
        return AccessRules(
            collectionAccess = CollectionAccessRule.everything(),
            videoAccess = VideoAccess.Everything
        )
    }
}

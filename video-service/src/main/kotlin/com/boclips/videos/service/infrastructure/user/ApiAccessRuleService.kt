package com.boclips.videos.service.infrastructure.user

import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.users.api.httpclient.UsersClient
import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.contentpartner.ContentPartnerId
import com.boclips.videos.service.domain.service.user.AccessRuleService
import feign.FeignException
import mu.KLogging
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable

open class ApiAccessRuleService(private val usersClient: UsersClient) :
    AccessRuleService {
    companion object : KLogging()

    @Retryable(
        maxAttempts = 3,
        backoff = Backoff(multiplier = 1.5)
    )
    override fun getRules(user: User): AccessRules {
        val retrievedAccessRules: List<AccessRuleResource> =
            try {
                usersClient.getAccessRulesOfUser(user.id.value)._embedded.accessRules
            } catch (ex: FeignException) {
                when (ex.status()) {
                    404 -> emptyList()
                    else -> throw ex
                }
            }

        val accessRules = retrievedAccessRules.let {
            AccessRules(
                collectionAccess = getCollectionAccessRule(it, user),
                videoAccess = getVideoAccessRule(it)
            )
        }

        logger.info { "Retrieved access rules for user ${user.id.value}" }
        return accessRules
    }

    private fun getCollectionAccessRule(accessRules: List<AccessRuleResource>, user: User): CollectionAccessRule {
        val collectionIds: List<CollectionId> = accessRules
            .flatMap { accessRule ->
                when (accessRule) {
                    is AccessRuleResource.IncludedCollections -> accessRule.collectionIds.map { CollectionId(it) }
                    else -> emptyList()
                }
            }

        return when {
            user.isPermittedToModifyAnyCollection -> CollectionAccessRule.everything()
            collectionIds.isNotEmpty() -> CollectionAccessRule.specificIds(collectionIds)
            collectionIds.isEmpty() -> CollectionAccessRule.everything() // TODO: remove this rule and replace with access rule
            else -> CollectionAccessRule.asOwner(user.id)
        }
    }

    private fun getVideoAccessRule(accessRules: List<AccessRuleResource>): VideoAccess {
        val videoAccessRules = accessRules.mapNotNull {
            when (it) {
                is AccessRuleResource.IncludedDistributionMethod -> VideoAccessRule.IncludedDistributionMethods(
                    it.distributionMethods.map { method -> DistributionMethod.valueOf(method) }.toSet()
                )
                is AccessRuleResource.IncludedVideos -> VideoAccessRule.IncludedIds(
                    it.videoIds.map { id -> VideoId(id) }.toSet()
                )
                is AccessRuleResource.ExcludedVideos -> VideoAccessRule.ExcludedIds(
                    it.videoIds.map { id -> VideoId(id) }.toSet()
                )
                is AccessRuleResource.ExcludedVideoTypes -> extractExcludedContentTypes(it)
                is AccessRuleResource.ExcludedContentPartners -> VideoAccessRule.ExcludedContentPartners(
                    it.contentPartnerIds.map { id ->
                        ContentPartnerId(
                            id
                        )
                    }.toSet()
                )
                is AccessRuleResource.IncludedCollections -> null
            }
        }

        return when {
            videoAccessRules.isNotEmpty() -> VideoAccess.Rules(videoAccessRules)
            else -> VideoAccess.Everything
        }
    }

    private fun extractExcludedContentTypes(accessRule: AccessRuleResource.ExcludedVideoTypes): VideoAccessRule.ExcludedContentTypes? =
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
            collectionAccess = CollectionAccessRule.everything(),
            videoAccess = VideoAccess.Everything
        )
    }
}

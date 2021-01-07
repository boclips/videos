package com.boclips.videos.service.infrastructure.user

import com.boclips.users.api.httpclient.OrganisationsClient
import com.boclips.users.api.httpclient.UsersClient
import com.boclips.users.api.response.organisation.DealResource
import com.boclips.users.api.response.organisation.OrganisationResource
import com.boclips.videos.service.domain.model.user.Deal
import com.boclips.videos.service.domain.model.user.Deal.Prices
import com.boclips.videos.service.domain.model.user.Organisation
import com.boclips.videos.service.domain.model.user.OrganisationId
import com.boclips.videos.service.domain.model.video.VideoType.INSTRUCTIONAL_CLIPS
import com.boclips.videos.service.domain.model.video.VideoType.NEWS
import com.boclips.videos.service.domain.model.video.VideoType.STOCK
import com.boclips.videos.service.domain.service.user.UserService
import feign.FeignException
import mu.KLogging
import org.springframework.cache.annotation.Cacheable
import java.math.BigDecimal
import java.util.Currency

open class ApiUserService(
    private val usersClient: UsersClient,
    private val organisationsClient: OrganisationsClient
) : UserService {

    companion object : KLogging()

    @Cacheable("user-subjects")
    override fun getSubjectIds(userId: String): Set<String>? {
        val user = try {
            usersClient.getUser(userId)
        } catch (e: Exception) {
            logger.error(e) { "Error fetching subjects for user $userId" }
            return null
        }
        return user.subjects?.map { subject -> subject.id }?.toSet()
    }

    override fun getOrganisationOfUser(userId: String): Organisation? {
        return try {
            usersClient.getUser(userId).organisation?.id
                ?.let { organisationsClient.getOrganisation(it) }
                ?.let { convertOrganisation(it) }
        } catch (ex: FeignException) {
            if (ex.status() != 404) {
                logger.error(ex) { "Error fetching organisation of user $userId" }
            }
            null
        }
    }

    override fun isShareCodeValid(referrerId: String, shareCode: String): Boolean {
        return try {
            usersClient.getShareCode(referrerId, shareCode)
            true
        } catch (e: FeignException) {
            when (e.status()) {
                403 -> logger.info { "Invalid share code $shareCode for user $referrerId" }
                else -> logger.error(e) { "Could not determining whether share code $shareCode is valid for $referrerId" }
            }
            false
        }
    }

    private fun convertOrganisation(it: OrganisationResource): Organisation {
        return Organisation(
            organisationId = OrganisationId(it.id),
            allowOverridingUserIds = it.organisationDetails.allowsOverridingUserIds ?: false,
            deal = Deal(
                prices = Prices(
                    videoTypePrices = it.deal?.prices?.videoTypePrices?.entries?.map { price ->
                        when (price.key) {
                            "INSTRUCTIONAL" -> INSTRUCTIONAL_CLIPS to buildPrice(price.value)
                            "NEWS" -> NEWS to buildPrice(price.value)
                            "STOCK" -> STOCK to buildPrice(price.value)
                            else -> throw RuntimeException("Unsupported key for videoTypePrices JSON object: ${price.key}")
                        }
                    }?.toMap() ?: emptyMap()
                )
            )
        )
    }

    private fun buildPrice(it: DealResource.PriceResource) =
        Prices.Price(BigDecimal(it.amount), Currency.getInstance(it.currency))
}

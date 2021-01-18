package com.boclips.videos.service.infrastructure.organisation

import com.boclips.users.api.response.organisation.DealResource
import com.boclips.users.api.response.organisation.OrganisationResource
import com.boclips.videos.service.domain.model.user.Deal
import com.boclips.videos.service.domain.model.user.Organisation
import com.boclips.videos.service.domain.model.user.OrganisationId
import com.boclips.videos.service.domain.model.video.VideoType
import java.math.BigDecimal
import java.util.Currency

class OrganisationResourceConverter {
    companion object {
        fun convertOrganisation(it: OrganisationResource): Organisation {
            return Organisation(
                organisationId = OrganisationId(it.id),
                allowOverridingUserIds = it.organisationDetails.allowsOverridingUserIds ?: false,
                deal = Deal(
                    prices = Deal.Prices(
                        videoTypePrices = it.deal?.prices?.videoTypePrices?.entries?.map { price ->
                            when (price.key) {
                                "INSTRUCTIONAL" -> VideoType.INSTRUCTIONAL_CLIPS to buildPrice(price.value)
                                "NEWS" -> VideoType.NEWS to buildPrice(price.value)
                                "STOCK" -> VideoType.STOCK to buildPrice(price.value)
                                else -> throw RuntimeException("Unsupported key for videoTypePrices JSON object: ${price.key}")
                            }
                        }?.toMap() ?: emptyMap()
                    )
                )
            )
        }

        private fun buildPrice(it: DealResource.PriceResource) =
            Deal.Prices.Price(BigDecimal(it.amount), Currency.getInstance(it.currency))
    }
}

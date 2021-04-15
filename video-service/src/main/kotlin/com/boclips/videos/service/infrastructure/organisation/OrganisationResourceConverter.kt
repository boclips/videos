package com.boclips.videos.service.infrastructure.organisation

import com.boclips.users.api.response.organisation.DealResource
import com.boclips.users.api.response.organisation.OrganisationResource
import com.boclips.videos.service.domain.model.user.Deal
import com.boclips.videos.service.domain.model.user.Organisation
import com.boclips.videos.service.domain.model.user.OrganisationId
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import java.math.BigDecimal
import java.util.*

class OrganisationResourceConverter {
    companion object {
        private val NO_CUSTOM_PRICES = Deal.Prices(videoTypePrices = emptyMap(), channelPrices = emptyMap())

        fun convertOrganisation(it: OrganisationResource): Organisation {
            return Organisation(
                organisationId = OrganisationId(it.id),
                allowOverridingUserIds = it.organisationDetails.allowsOverridingUserIds ?: false,
                deal = Deal(
                    prices = it.deal.prices?.let { prices ->
                        Deal.Prices(
                            videoTypePrices = convertToVideoTypePrices(prices),
                            channelPrices = convertToChannelPrices(prices)
                        )
                    } ?: NO_CUSTOM_PRICES
                ),
                features = it.organisationDetails.features ?: emptyMap()
            )
        }

        private fun convertToChannelPrices(prices: DealResource.PricesResource) =
            prices.channelPrices.map {
                ChannelId(it.key) to buildPrice(it.value)
            }.toMap()

        private fun convertToVideoTypePrices(prices: DealResource.PricesResource) =
            prices.videoTypePrices.map { price ->
                when (price.key) {
                    "INSTRUCTIONAL" -> VideoType.INSTRUCTIONAL_CLIPS to buildPrice(price.value)
                    "NEWS" -> VideoType.NEWS to buildPrice(price.value)
                    "STOCK" -> VideoType.STOCK to buildPrice(price.value)
                    else -> throw RuntimeException("Unsupported key for videoTypePrices JSON object: ${price.key}")
                }
            }.toMap()

        private fun buildPrice(it: DealResource.PriceResource) =
            Deal.Prices.Price(BigDecimal(it.amount), Currency.getInstance(it.currency))
    }
}

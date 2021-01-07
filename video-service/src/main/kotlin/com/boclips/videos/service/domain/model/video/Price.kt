package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.user.Deal.Prices.Price as OrganisationPrice
import com.boclips.videos.service.domain.model.user.Deal.Prices
import com.boclips.videos.service.domain.model.video.VideoType.*
import java.math.BigDecimal
import java.util.Currency

data class Price(val amount: BigDecimal, val currency: Currency = Currency.getInstance("USD")) {

    companion object {
        private val USD = Currency.getInstance("USD")
        private val DEFAULT_PRICES = mapOf(
                INSTRUCTIONAL_CLIPS to Price(amount = BigDecimal(600), currency = USD),
                NEWS to Price(amount = BigDecimal(300), currency = USD),
                STOCK to Price(amount = BigDecimal(150), currency = USD)
        )

        fun computePrice(videoTypes: List<VideoType>, prices: Prices?): Price? {
            val videoTypePrices = prices?.videoTypePrices ?: emptyMap()
            return videoTypes
                    .map { priceForVideoType(it, videoTypePrices) }
                    .requireNoNulls()
                    .maxWithOrNull(compareBy { it.amount })
        }

        private fun priceForVideoType(videoType: VideoType, videoTypePrices: Map<VideoType, OrganisationPrice>): Price? {
            return buildPrice(videoTypePrices[videoType]) ?: DEFAULT_PRICES[videoType]
        }

        private fun buildPrice(organisationPrice: OrganisationPrice?): Price? {
            return organisationPrice?.let {
                Price(
                        amount = organisationPrice.amount,
                        currency = organisationPrice.currency
                )
            }
        }
    }
}

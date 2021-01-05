package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.user.Organisation.Deal.VideoTypePrices
import com.boclips.videos.service.domain.model.user.Organisation.Deal.VideoTypePrices.Price as OrganisationPrice
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

        fun computePrice(videoTypes: List<VideoType>, videoTypesPrices: VideoTypePrices?): Price? {
            return videoTypes
                    .map { priceForVideoType(it, videoTypesPrices) }
                    .requireNoNulls()
                    .maxWithOrNull(compareBy { it.amount })
        }

        private fun priceForVideoType(videoType: VideoType, videoTypesPrices: VideoTypePrices?): Price? {
            return videoTypesPrices
                    ?.let { buildPrice(it[videoType]) }
                    ?: DEFAULT_PRICES[videoType]
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

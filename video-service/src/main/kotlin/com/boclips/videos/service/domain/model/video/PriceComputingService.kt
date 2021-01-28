package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.user.Deal.Prices
import com.boclips.videos.service.domain.model.user.Organisation
import com.boclips.videos.service.domain.model.user.OrganisationsPrices
import com.boclips.videos.service.domain.model.video.VideoType.*
import java.math.BigDecimal
import java.util.Currency
import com.boclips.videos.service.domain.model.user.Deal.Prices.Price as OrganisationPrice

class PriceComputingService {

    fun computeVideoPrice(video: Video, organisationPrices: Prices?): Price? {
        return if (video.isBoclipsHosted()) {
            if (video.types.isEmpty())
                throw VideoMissingTypeException(video.videoId)
            computeVideoTypePrice(
                video.types,
                zipPricesWithDefaultPrices(customPrices = organisationPrices?.videoTypePrices)
            )
        } else {
            null
        }
    }

    fun computeVideoOrganisationPrices(video: Video, organisationsPrices: List<Organisation>): OrganisationsPrices? {
        if (!video.isBoclipsHosted()) {
            return null
        }

        val videoPrices = organisationsPrices.mapNotNull { organisation ->
            computeVideoPrice(video, organisation.deal.prices)?.let { price ->
                organisation.organisationId to price
            }
        }.toMap()

        return OrganisationsPrices(
            prices = videoPrices,
            default = computeVideoTypePrice(video.types, DEFAULT_VIDEO_TYPE_PRICES)!!
        )
    }

    private fun zipPricesWithDefaultPrices(customPrices: Map<VideoType, OrganisationPrice>?): Map<VideoType, OrganisationPrice> {
        return DEFAULT_VIDEO_TYPE_PRICES.map { price ->
            if (customPrices?.get(price.key) != null) {
                // We can't avoid this bang bang because kotlin thinks custom prices could be null, even though we just checked
                price.key to customPrices[price.key]!!
            } else {
                price.key to price.value
            }
        }.toMap()
    }

    private fun computeVideoTypePrice(
        videoTypes: List<VideoType>,
        videoTypePrices: Map<VideoType, OrganisationPrice>
    ): Price? {
        return videoTypes
            .map { priceForVideoType(it, videoTypePrices) }
            .requireNoNulls()
            .maxWithOrNull(compareBy { it.amount })
    }

    private fun priceForVideoType(
        videoType: VideoType,
        videoTypePrices: Map<VideoType, OrganisationPrice>
    ): Price? {
        return buildPrice(videoTypePrices[videoType])
    }

    private fun buildPrice(organisationPrice: OrganisationPrice?): Price? {
        return organisationPrice?.let {
            Price(
                amount = organisationPrice.amount,
                currency = organisationPrice.currency
            )
        }
    }

    companion object {
        private val USD = Currency.getInstance("USD")
        val DEFAULT_VIDEO_TYPE_PRICES: Map<VideoType, OrganisationPrice> = mapOf(
            INSTRUCTIONAL_CLIPS to OrganisationPrice(amount = BigDecimal(600), currency = USD),
            NEWS to OrganisationPrice(amount = BigDecimal(300), currency = USD),
            STOCK to OrganisationPrice(amount = BigDecimal(150), currency = USD)
        )
    }
}

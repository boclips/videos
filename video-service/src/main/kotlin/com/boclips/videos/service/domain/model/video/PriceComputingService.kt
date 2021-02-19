package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.user.Deal.Prices
import com.boclips.videos.service.domain.model.user.Organisation
import com.boclips.videos.service.domain.model.user.OrganisationsPrices
import com.boclips.videos.service.domain.model.video.VideoType.*
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import java.math.BigDecimal
import java.util.*
import com.boclips.videos.service.domain.model.user.Deal.Prices.Price as OrganisationPrice

class PriceComputingService {

    fun computeVideoPrice(
        videoId: VideoId,
        organisationPrices: Prices?,
        playback: VideoPlayback,
        channel: ChannelId,
        videoTypes: List<VideoType>
    ): Price? {
        if (videoTypes.isEmpty()) {
            throw VideoMissingTypeException(videoId)
        }
        return if (playback is VideoPlayback.StreamPlayback) {
            computeVideoChannelPrice(channel, organisationPrices?.channelPrices)
                ?: computeVideoTypePrice(
                    videoTypes,
                    zipPricesWithDefaultPrices(customPrices = organisationPrices?.videoTypePrices)
                )
        } else {
            null
        }
    }

    fun computeVideoOrganisationPrices(
        videoId: VideoId,
        organisationsPrices: List<Organisation>,
        playback: VideoPlayback,
        channel: ChannelId,
        videoTypes: List<VideoType>
    ): OrganisationsPrices? {
        if (playback is VideoPlayback.YoutubePlayback) {
            return null
        }

        val videoPrices = organisationsPrices.mapNotNull { organisation ->
            computeVideoPrice(
                videoId = videoId,
                organisationPrices = organisation.deal.prices,
                channel = channel,
                playback = playback,
                videoTypes = videoTypes
            )?.let { price ->
                organisation.organisationId to price
            }
        }.toMap()

        return OrganisationsPrices(
            prices = videoPrices,
            default = computeVideoTypePrice(videoTypes, DEFAULT_VIDEO_TYPE_PRICES)!!
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

    private fun computeVideoChannelPrice(
        channel: ChannelId,
        channelPrices: Map<ChannelId, OrganisationPrice>?
    ): Price? {
        if (channelPrices == null) {
            return null
        }

        return buildPrice(channelPrices[channel])
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

package com.boclips.videos.service.testsupport

import com.boclips.videos.service.domain.model.user.Deal
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.VideoType.*
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import java.math.BigDecimal
import java.util.*

object DealPricesFactory {
    private val USD = Currency.getInstance("USD")

    fun sample(
        videoTypePrices: Map<VideoType, Deal.Prices.Price> = sampleVideoTypePrices(),
        channelPrices: Map<ChannelId, Deal.Prices.Price> = sampleChannelPrices(),
    ) = Deal.Prices(
        videoTypePrices = videoTypePrices,
        channelPrices = channelPrices
    )

    fun sampleVideoTypePrices(
        instructional: BigDecimal = BigDecimal.valueOf(600),
        news: BigDecimal = BigDecimal.valueOf(300),
        stock: BigDecimal = BigDecimal.valueOf(150),
        currency: Currency = USD

    ): Map<VideoType, Deal.Prices.Price> = mapOf(
        INSTRUCTIONAL_CLIPS to Deal.Prices.Price(instructional, currency),
        NEWS to Deal.Prices.Price(news, currency),
        STOCK to Deal.Prices.Price(stock, currency)
    )

    fun sampleChannelPrices(vararg priceMapping: Pair<ChannelId, Deal.Prices.Price>): Map<ChannelId, Deal.Prices.Price> =
        if (priceMapping.isNotEmpty()) {
            mapOf(*priceMapping)
        } else {
            mapOf(ChannelId("ted-ed") to Deal.Prices.Price(BigDecimal.valueOf(150), USD))
        }
}

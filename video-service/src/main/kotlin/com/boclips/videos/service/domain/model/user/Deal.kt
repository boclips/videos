package com.boclips.videos.service.domain.model.user

import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import java.math.BigDecimal
import java.util.Currency

data class Deal(val prices: Prices) {
    data class Prices(
        val videoTypePrices: Map<VideoType, Price>,
        val channelPrices: Map<ChannelId, Price>
    ) {
        data class Price(
            val amount: BigDecimal,
            val currency: Currency
        )
    }
}

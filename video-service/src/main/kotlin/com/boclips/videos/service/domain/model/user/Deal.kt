package com.boclips.videos.service.domain.model.user

import com.boclips.videos.service.domain.model.video.VideoType
import java.math.BigDecimal
import java.util.*

data class Deal(val prices: Prices) {
    data class Prices(
        val videoTypePrices: Map<VideoType, Price>
    ) {

        data class Price(
                val amount: BigDecimal,
                val currency: Currency
        )
    }
}

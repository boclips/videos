package com.boclips.videos.service.testsupport

import com.boclips.videos.service.domain.model.user.Deal
import com.boclips.videos.service.domain.model.video.VideoType.INSTRUCTIONAL_CLIPS
import com.boclips.videos.service.domain.model.video.VideoType.NEWS
import com.boclips.videos.service.domain.model.video.VideoType.STOCK
import java.math.BigDecimal
import java.util.Currency

object PriceFactory {

    private val USD = Currency.getInstance("USD")

    fun sample(
        instructional: BigDecimal = BigDecimal.valueOf(600),
        news: BigDecimal = BigDecimal.valueOf(300),
        stock: BigDecimal = BigDecimal.valueOf(150),
        currency: Currency = USD
    ) = Deal.Prices(
        videoTypePrices = mapOf(
            INSTRUCTIONAL_CLIPS to Deal.Prices.Price(instructional, currency),
            NEWS to Deal.Prices.Price(news, currency),
            STOCK to Deal.Prices.Price(stock, currency)
        )
    )
}

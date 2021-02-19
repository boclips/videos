package com.boclips.videos.api.response.video

import java.math.BigDecimal
import java.util.Currency

data class PriceResource(
    val amount: BigDecimal,
    val currency: Currency
)

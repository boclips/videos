package com.boclips.videos.api.response.video

import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale

data class PriceResource(
    val amount: BigDecimal,
    val currency: Currency
)

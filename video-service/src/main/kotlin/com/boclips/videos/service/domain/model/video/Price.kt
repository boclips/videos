package com.boclips.videos.service.domain.model.video

import java.math.BigDecimal
import java.util.Currency

data class Price(
    val amount: BigDecimal,
    val currency: Currency = Currency.getInstance("USD")
)

package com.boclips.videos.service.presentation.converters

import java.math.BigDecimal

object PriceConverter {
    fun toPrice(price: String): BigDecimal {
        return BigDecimal(price).movePointLeft(2)
    }
}

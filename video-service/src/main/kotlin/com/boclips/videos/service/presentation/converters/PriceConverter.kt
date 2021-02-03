package com.boclips.videos.service.presentation.converters

import java.math.BigDecimal
import java.math.MathContext

object PriceConverter {
    fun toPrice(price: String): BigDecimal {
        return BigDecimal(price).movePointLeft(2)
    }
}

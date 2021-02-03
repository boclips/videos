package com.boclips.videos.service.presentation.converters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.NumberFormatException
import java.math.BigDecimal

internal class PriceConverterTest {
    @Test
    fun `it converts valid strings to prices with decimal place in correct position`() {
        val priceString = "10000"
        val price = PriceConverter.toPrice(priceString)

        assertThat(price.toPlainString()).isEqualTo("100.00")
        assertThat(price).isEqualTo(BigDecimal.valueOf(10000, 2))
    }

    @Test
    fun `it can handle larger numbers`() {
        val priceString = "10000999"
        val price = PriceConverter.toPrice(priceString)

        assertThat(price.toPlainString()).isEqualTo("100009.99")
        assertThat(price).isEqualTo(BigDecimal.valueOf(100009.99))
    }

    @Test
    fun `it throws when an invalid value is provided`() {
        assertThrows<NumberFormatException>{
            PriceConverter.toPrice("not a number")
        }
    }
}

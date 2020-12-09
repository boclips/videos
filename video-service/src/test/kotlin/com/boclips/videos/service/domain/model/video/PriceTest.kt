package com.boclips.videos.service.domain.model.video

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Currency

class PriceTest {

    @Test
    fun `get default price for INSTRUCTIONAL_CLIPS`() {
        val price = Price.getDefault(listOf(ContentType.INSTRUCTIONAL_CLIPS))

        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
        assertThat(price.amount.intValueExact()).isEqualTo(600)
    }

    @Test
    fun `get default price for NEWS`() {
        val price = Price.getDefault(listOf(ContentType.NEWS))

        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
        assertThat(price.amount.intValueExact()).isEqualTo(300)
    }

    @Test
    fun `get default price for STOCK`() {
        val price = Price.getDefault(listOf(ContentType.STOCK))

        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
        assertThat(price.amount.intValueExact()).isEqualTo(150)
    }

    @Test
    fun `gets the most expensive price when multiple content types`() {
        val price = Price.getDefault(listOf(ContentType.STOCK, ContentType.NEWS))

        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
        assertThat(price.amount.intValueExact()).isEqualTo(300)
    }

    @Test
    fun `defaults to the most expensive type when no content types specified`() {
        val price = Price.getDefault(emptyList())

        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
        assertThat(price.amount.intValueExact()).isEqualTo(600)
    }
}

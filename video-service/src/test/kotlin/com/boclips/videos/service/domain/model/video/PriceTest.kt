package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.testsupport.DealPricesFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

class PriceTest {

    private val USD = Currency.getInstance("USD")
    
    @Nested
    inner class OrganisationVideoTypePrices {

        @Test
        fun `get organisation price for INSTRUCTIONAL_CLIPS`() {
            val prices = DealPricesFactory.sample(
                    instructional = BigDecimal.valueOf(600),
                    news = BigDecimal.ONE,
                    stock = BigDecimal.ZERO
            )
            val price = Price.computePrice(listOf(VideoType.INSTRUCTIONAL_CLIPS), prices = prices)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(600)
        }

        @Test
        fun `get organisation price for NEWS`() {
            val prices = DealPricesFactory.sample(
                    instructional = BigDecimal.TEN,
                    news = BigDecimal.valueOf(300),
                    stock = BigDecimal.ZERO
            )
            val price = Price.computePrice(listOf(VideoType.NEWS), prices = prices)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(300)
        }

        @Test
        fun `get organisation price for STOCK`() {
            val prices = DealPricesFactory.sample(
                    instructional = BigDecimal.TEN,
                    news = BigDecimal.ONE,
                    stock = BigDecimal.valueOf(150)
            )
            val price = Price.computePrice(listOf(VideoType.STOCK), prices = prices)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(150)
        }

        @Test
        fun `gets the most expensive price from organisation ones when multiple content types`() {
            val prices = DealPricesFactory.sample(
                    instructional = BigDecimal.valueOf(7000),
                    news = BigDecimal.valueOf(300),
                    stock = BigDecimal.valueOf(150)
            )
            val price = Price.computePrice(listOf(VideoType.STOCK, VideoType.NEWS), prices = prices)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(300)
        }

        @Test
        fun `returns no price when no video type is given`() {
            val prices = DealPricesFactory.sample(
                    instructional = BigDecimal.valueOf(600),
                    news = BigDecimal.valueOf(300),
                    stock = BigDecimal.valueOf(1500)
            )
            val result = Price.computePrice(videoTypes = emptyList(), prices = prices)

            assertThat(result).isNull()
        }
    }

    @Nested
    inner class DefaultPrices {

        @Test
        fun `get default price for INSTRUCTIONAL_CLIPS`() {
            val price = Price.computePrice(listOf(VideoType.INSTRUCTIONAL_CLIPS), prices = null)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(600)
        }

        @Test
        fun `get default price for NEWS`() {
            val price = Price.computePrice(listOf(VideoType.NEWS), prices = null)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(300)
        }

        @Test
        fun `get default price for STOCK`() {
            val price = Price.computePrice(listOf(VideoType.STOCK), prices = null)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(150)
        }

        @Test
        fun `gets the most expensive price when multiple content types`() {
            val price = Price.computePrice(listOf(VideoType.STOCK, VideoType.NEWS), prices = null)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(300)
        }

        @Test
        fun `defaults to the most expensive type when no content types specified`() {
            val price = Price.computePrice(emptyList(), prices = null)

            assertThat(price).isNull()
        }
    }
}

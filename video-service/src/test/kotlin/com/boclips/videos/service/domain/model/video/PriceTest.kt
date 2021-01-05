package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.user.Organisation.Deal.VideoTypePrices
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import com.boclips.videos.service.domain.model.user.Organisation.Deal.VideoTypePrices.Price as OrganisationPrice

class PriceTest {

    private val USD = Currency.getInstance("USD")
    
    @Nested
    inner class OrganisationVideoTypePrices {

        @Test
        fun `get organisation price for INSTRUCTIONAL_CLIPS`() {
            val videoTypesPrices = VideoTypePrices(
                    instructional = OrganisationPrice(BigDecimal.valueOf(600), USD),
                    news = OrganisationPrice(BigDecimal.ONE, USD),
                    stock = OrganisationPrice(BigDecimal.ZERO, USD)
            )
            val price = Price.computePrice(listOf(VideoType.INSTRUCTIONAL_CLIPS), videoTypesPrices = videoTypesPrices)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(600)
        }

        @Test
        fun `get organisation price for NEWS`() {
            val videoTypesPrices = VideoTypePrices(
                    instructional = OrganisationPrice(BigDecimal.TEN, USD),
                    news = OrganisationPrice(BigDecimal.valueOf(300), USD),
                    stock = OrganisationPrice(BigDecimal.ZERO, USD)
            )
            val price = Price.computePrice(listOf(VideoType.NEWS), videoTypesPrices = videoTypesPrices)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(300)
        }

        @Test
        fun `get organisation price for STOCK`() {
            val videoTypesPrices = VideoTypePrices(
                    instructional = OrganisationPrice(BigDecimal.TEN, USD),
                    news = OrganisationPrice(BigDecimal.ONE, USD),
                    stock = OrganisationPrice(BigDecimal.valueOf(150), USD)
            )
            val price = Price.computePrice(listOf(VideoType.STOCK), videoTypesPrices = videoTypesPrices)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(150)
        }

        @Test
        fun `gets the most expensive price from organisation ones when multiple content types`() {
            val videoTypesPrices = VideoTypePrices(
                    instructional = OrganisationPrice(BigDecimal.valueOf(7000), USD),
                    news = OrganisationPrice(BigDecimal.valueOf(300), USD),
                    stock = OrganisationPrice(BigDecimal.valueOf(150), USD)
            )
            val price = Price.computePrice(listOf(VideoType.STOCK, VideoType.NEWS), videoTypesPrices = videoTypesPrices)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(300)
        }

        @Test
        fun `returns no price when no video type is given`() {
            val videoTypesPrices = VideoTypePrices(
                    instructional = OrganisationPrice(BigDecimal.valueOf(600), USD),
                    news = OrganisationPrice(BigDecimal.valueOf(300), USD),
                    stock = OrganisationPrice(BigDecimal.valueOf(1500), USD)
            )
            val result = Price.computePrice(videoTypes = emptyList(), videoTypesPrices = videoTypesPrices)

            assertThat(result).isNull()
        }
    }

    @Nested
    inner class DefaultPrices {

        @Test
        fun `get default price for INSTRUCTIONAL_CLIPS`() {
            val price = Price.computePrice(listOf(VideoType.INSTRUCTIONAL_CLIPS), videoTypesPrices = null)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(600)
        }

        @Test
        fun `get default price for NEWS`() {
            val price = Price.computePrice(listOf(VideoType.NEWS), videoTypesPrices = null)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(300)
        }

        @Test
        fun `get default price for STOCK`() {
            val price = Price.computePrice(listOf(VideoType.STOCK), videoTypesPrices = null)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(150)
        }

        @Test
        fun `gets the most expensive price when multiple content types`() {
            val price = Price.computePrice(listOf(VideoType.STOCK, VideoType.NEWS), videoTypesPrices = null)!!

            assertThat(price.currency).isEqualTo(USD)
            assertThat(price.amount.intValueExact()).isEqualTo(300)
        }

        @Test
        fun `defaults to the most expensive type when no content types specified`() {
            val price = Price.computePrice(emptyList(), videoTypesPrices = null)

            assertThat(price).isNull()
        }
    }
}

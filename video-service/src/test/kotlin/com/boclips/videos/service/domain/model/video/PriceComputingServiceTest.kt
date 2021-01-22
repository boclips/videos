package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.user.Deal
import com.boclips.videos.service.domain.model.user.OrganisationId
import com.boclips.videos.service.testsupport.DealPricesFactory
import com.boclips.videos.service.testsupport.OrganisationFactory
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*

class PriceComputingServiceTest {

    private val priceComputingService = PriceComputingService()

    @Test
    fun `computes prices based on user's organisation video type prices`() {
        val video = TestFactories.createVideo(types = listOf(VideoType.NEWS))
        val organisationPrices = DealPricesFactory.sample(
            instructional = BigDecimal.valueOf(600),
            news = BigDecimal.ONE,
            stock = BigDecimal.ZERO
        )
        val price = priceComputingService.computeVideoPrice(video, organisationPrices)

        assertThat(price!!.amount).isEqualTo(BigDecimal.ONE)
        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
    }

    @Test
    fun `throws when no types are defined on a video when getting price`() {
        val video = TestFactories.createVideo(types = emptyList())
        val organisationPrices = DealPricesFactory.sample(
            instructional = BigDecimal.valueOf(600),
            news = BigDecimal.ONE,
            stock = BigDecimal.ZERO
        )

        assertThrows<VideoMissingTypeException> {
            priceComputingService.computeVideoPrice(video, organisationPrices)
        }
    }

    @Test
    fun `does not calculate for non boclips hosted video`() {
        val video =
            TestFactories.createVideo(playback = TestFactories.createYoutubePlayback(), types = listOf(VideoType.NEWS))
        val organisationPrices = DealPricesFactory.sample(
            instructional = BigDecimal.valueOf(600),
            news = BigDecimal.ONE,
            stock = BigDecimal.ZERO
        )

        val price = priceComputingService.computeVideoPrice(video, organisationPrices)
        assertThat(price).isNull()
    }

    @Test
    fun `does not retrieve organisation prices for a non-boclips hosted video`() {
        val video =
            TestFactories.createVideo(playback = TestFactories.createYoutubePlayback(), types = listOf(VideoType.NEWS))

        val organisation = OrganisationFactory.sample(
            organisationId = OrganisationId("org-id"),
            deal = Deal(prices = DealPricesFactory.sample(stock = BigDecimal.TEN))
        )

        val prices = priceComputingService.computeVideoOrganisationPrices(video, listOf(organisation))
        assertThat(prices).isNull()
    }

    @Test
    fun `retrieves organisation prices for a video`() {
        val video = TestFactories.createVideo(types = listOf(VideoType.STOCK))

        val organisation = OrganisationFactory.sample(
            organisationId = OrganisationId("org-id"),
            deal = Deal(prices = DealPricesFactory.sample(stock = BigDecimal.TEN))
        )
        val organisation2 = OrganisationFactory.sample(
            organisationId = OrganisationId("org-id1"),
            deal = Deal(prices = DealPricesFactory.sample(stock = BigDecimal.ONE))
        )
        val organisation3 = OrganisationFactory.sample(
            organisationId = OrganisationId("org-id2"),
            deal = Deal(
                prices = Deal.Prices(
                    videoTypePrices = mapOf(
                        VideoType.INSTRUCTIONAL_CLIPS to Deal.Prices.Price(
                            amount = BigDecimal.valueOf(1000),
                            currency = Currency.getInstance("USD")
                        )
                    )
                )
            )
        )

        val prices = priceComputingService.computeVideoOrganisationPrices(
            video,
            listOf(organisation, organisation2, organisation3)
        )!!

        assertThat(prices.prices).hasSize(3)
        assertThat(prices.prices?.get(organisation.organisationId)?.amount).isEqualTo(BigDecimal.TEN)
        assertThat(prices.prices?.get(organisation2.organisationId)?.amount).isEqualTo(BigDecimal.ONE)
        assertThat(prices.prices?.get(organisation3.organisationId)?.amount)
            .isEqualTo(PriceComputingService.DEFAULT_VIDEO_TYPE_PRICES[VideoType.STOCK]?.amount)
    }

    @Test
    fun `retrieves organisation and default prices for a video`() {
        val video = TestFactories.createVideo(types = listOf(VideoType.STOCK))

        val organisation = OrganisationFactory.sample(
            organisationId = OrganisationId("org-id"),
            deal = Deal(prices = DealPricesFactory.sample(stock = BigDecimal.TEN))
        )
        val organisation2 = OrganisationFactory.sample(
            organisationId = OrganisationId("org-id1"),
            deal = Deal(prices = DealPricesFactory.sample(stock = BigDecimal.ONE))
        )

        val prices = priceComputingService.computeVideoOrganisationPrices(video, listOf(organisation, organisation2))!!

        assertThat(prices.prices).hasSize(2)
        assertThat(prices.prices?.get(organisation.organisationId)?.amount).isEqualTo(BigDecimal.TEN)
        assertThat(prices.prices?.get(organisation2.organisationId)?.amount).isEqualTo(BigDecimal.ONE)
        assertThat(prices.default.amount).isEqualTo(BigDecimal(150))
    }
}

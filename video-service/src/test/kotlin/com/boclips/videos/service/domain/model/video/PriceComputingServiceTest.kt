package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.user.Deal
import com.boclips.videos.service.domain.model.user.OrganisationId
import com.boclips.videos.service.testsupport.DealPricesFactory
import com.boclips.videos.service.testsupport.OrganisationFactory
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions
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

        Assertions.assertThat(price!!.amount).isEqualTo(BigDecimal.ONE)
        Assertions.assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
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
        val video = TestFactories.createVideo(playback = TestFactories.createYoutubePlayback(), types = listOf(VideoType.NEWS))
        val organisationPrices = DealPricesFactory.sample(
                instructional = BigDecimal.valueOf(600),
                news = BigDecimal.ONE,
                stock = BigDecimal.ZERO
        )

        val price = priceComputingService.computeVideoPrice(video, organisationPrices)
        Assertions.assertThat(price).isNull()
    }

    @Test
    fun `does not retrieve organisation prices for a non-boclips hosted video`() {
        val video = TestFactories.createVideo(playback = TestFactories.createYoutubePlayback(), types = listOf(VideoType.NEWS))

        val organisation = OrganisationFactory.sample(
                organisationId = OrganisationId("org-id"),
                deal = Deal(prices = DealPricesFactory.sample(stock = BigDecimal.TEN))
        )

        val prices = priceComputingService.computeVideoOrganisationPrices(video, listOf(organisation))
        Assertions.assertThat(prices).isNull()
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

        val prices = priceComputingService.computeVideoOrganisationPrices(video, listOf(organisation, organisation2))!!

        Assertions.assertThat(prices).hasSize(2)
        Assertions.assertThat(prices[organisation.organisationId]?.amount).isEqualTo(BigDecimal.TEN)
        Assertions.assertThat(prices[organisation2.organisationId]?.amount).isEqualTo(BigDecimal.ONE)
    }
}

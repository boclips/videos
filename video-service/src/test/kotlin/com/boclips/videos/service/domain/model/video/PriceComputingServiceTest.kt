package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.user.Deal
import com.boclips.videos.service.domain.model.user.OrganisationId
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.testsupport.DealPricesFactory
import com.boclips.videos.service.testsupport.OrganisationFactory
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*

class PriceComputingServiceTest {
    private val priceComputingService = PriceComputingService()

    @Nested
    inner class VideoPrice {
        @Test
        fun `computes prices based on user's organisation video type prices`() {
            val video = TestFactories.createVideo(types = listOf(VideoType.NEWS))
            val organisationPrices = DealPricesFactory.sample(
                videoTypePrices = DealPricesFactory.sampleVideoTypePrices(
                    instructional = BigDecimal.valueOf(600),
                    news = BigDecimal.ONE,
                    stock = BigDecimal.ZERO
                )
            )
            val price = priceComputingService.computeVideoPrice(
                videoId = video.videoId,
                videoTypes = video.types,
                playback = video.playback,
                channel = video.channel.channelId,
                organisationPrices = organisationPrices
            )

            assertThat(price!!.amount).isEqualTo(BigDecimal.ONE)
            assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
        }

        @Test
        fun `throws when no types are defined on a video when getting price`() {
            val video = TestFactories.createVideo(types = emptyList())
            val organisationPrices = DealPricesFactory.sample(
                videoTypePrices = DealPricesFactory.sampleVideoTypePrices(
                    instructional = BigDecimal.valueOf(600),
                    news = BigDecimal.ONE,
                    stock = BigDecimal.ZERO
                )
            )

            assertThrows<VideoMissingTypeException> {
                priceComputingService.computeVideoPrice(
                    videoId = video.videoId,
                    videoTypes = video.types,
                    playback = video.playback,
                    channel = video.channel.channelId,
                    organisationPrices = organisationPrices
                )
            }
        }

        @Test
        fun `does not calculate for non boclips hosted video`() {
            val video =
                TestFactories.createVideo(
                    playback = TestFactories.createYoutubePlayback(),
                    types = listOf(VideoType.NEWS)
                )
            val organisationPrices = DealPricesFactory.sample(
                videoTypePrices = DealPricesFactory.sampleVideoTypePrices(
                    instructional = BigDecimal.valueOf(600),
                    news = BigDecimal.ONE,
                    stock = BigDecimal.ZERO
                )
            )

            val price = priceComputingService.computeVideoPrice(
                videoId = video.videoId,
                videoTypes = video.types,
                playback = video.playback,
                channel = video.channel.channelId,
                organisationPrices = organisationPrices
            )
            assertThat(price).isNull()
        }

        @Test
        fun `channel prices takes precedence over type prices`() {
            val video =
                TestFactories.createVideo(types = listOf(VideoType.STOCK), channelId = ChannelId(value = "GME-MOON"))

            val organisationPrices = DealPricesFactory.sample(
                videoTypePrices = DealPricesFactory.sampleVideoTypePrices(stock = BigDecimal.valueOf(1000)),
                channelPrices = DealPricesFactory.sampleChannelPrices(
                    ChannelId(value = "GME-MOON") to Deal.Prices.Price(
                        BigDecimal.valueOf(150),
                        Currency.getInstance("USD")
                    )
                )
            )

            val price = priceComputingService.computeVideoPrice(
                videoId = video.videoId,
                videoTypes = video.types,
                playback = video.playback,
                channel = video.channel.channelId,
                organisationPrices = organisationPrices
            )!!
            assertThat(price.amount).isEqualTo(BigDecimal.valueOf(150))
        }

    }

    @Nested
    inner class OrganisationalPrices {
        @Test
        fun `does not retrieve organisation prices for a non-boclips hosted video`() {
            val video =
                TestFactories.createVideo(
                    playback = TestFactories.createYoutubePlayback(),
                    types = listOf(VideoType.NEWS)
                )

            val organisation = OrganisationFactory.sample(
                organisationId = OrganisationId("org-id"),
                deal = Deal(
                    prices = DealPricesFactory.sample(
                        videoTypePrices = DealPricesFactory.sampleVideoTypePrices(
                            stock = BigDecimal.TEN
                        )
                    )
                )
            )

            val prices = priceComputingService.computeVideoOrganisationPrices(
                organisationsPrices = listOf(organisation),
                videoId = video.videoId,
                videoTypes = video.types,
                playback = video.playback,
                channel = video.channel.channelId,
            )
            assertThat(prices).isNull()
        }

        @Test
        fun `retrieves organisation prices for a video`() {
            val video = TestFactories.createVideo(types = listOf(VideoType.STOCK))

            val organisation = OrganisationFactory.sample(
                organisationId = OrganisationId("org-id"),
                deal = Deal(
                    prices = DealPricesFactory.sample(
                        videoTypePrices = DealPricesFactory.sampleVideoTypePrices(
                            stock = BigDecimal.TEN
                        )
                    )
                )
            )
            val organisation2 = OrganisationFactory.sample(
                organisationId = OrganisationId("org-id1"),
                deal = Deal(
                    prices = DealPricesFactory.sample(
                        videoTypePrices = DealPricesFactory.sampleVideoTypePrices(
                            stock = BigDecimal.ONE
                        )
                    )
                )
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
                        ),
                        channelPrices = emptyMap()
                    )
                )
            )

            val prices = priceComputingService.computeVideoOrganisationPrices(
                organisationsPrices = listOf(organisation, organisation2, organisation3),
                videoId = video.videoId,
                videoTypes = video.types,
                playback = video.playback,
                channel = video.channel.channelId,

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
                deal = Deal(
                    prices = DealPricesFactory.sample(
                        videoTypePrices = DealPricesFactory.sampleVideoTypePrices(
                            stock = BigDecimal.TEN
                        )
                    )
                )
            )
            val organisation2 = OrganisationFactory.sample(
                organisationId = OrganisationId("org-id1"),
                deal = Deal(
                    prices = DealPricesFactory.sample(
                        videoTypePrices = DealPricesFactory.sampleVideoTypePrices(
                            stock = BigDecimal.ONE
                        )
                    )
                )
            )

            val prices = priceComputingService.computeVideoOrganisationPrices(
                organisationsPrices = listOf(organisation, organisation2),
                videoId = video.videoId,
                videoTypes = video.types,
                playback = video.playback,
                channel = video.channel.channelId,
            )!!
            assertThat(prices.prices).hasSize(2)
            assertThat(prices.prices?.get(organisation.organisationId)?.amount).isEqualTo(BigDecimal.TEN)
            assertThat(prices.prices?.get(organisation2.organisationId)?.amount).isEqualTo(BigDecimal.ONE)
            assertThat(prices.default.amount).isEqualTo(BigDecimal(150))
        }

        @Test
        fun `can retrieve channel custom pricing`() {
            val video = TestFactories.createVideo(types = listOf(VideoType.STOCK), channelId = ChannelId("getty"))

            val organisation = OrganisationFactory.sample(
                organisationId = OrganisationId("org-id"),
                deal = Deal(
                    prices = DealPricesFactory.sample(
                        channelPrices = DealPricesFactory.sampleChannelPrices(
                            ChannelId("getty") to Deal.Prices.Price(
                                BigDecimal.valueOf(1500),
                                Currency.getInstance("USD")
                            )
                        ),
                        videoTypePrices = mapOf(
                            VideoType.STOCK to Deal.Prices.Price(
                                BigDecimal.valueOf(1),
                                Currency.getInstance("USD")
                            )
                        )
                    )
                )
            )
            val organisation2 = OrganisationFactory.sample(
                organisationId = OrganisationId("org-id1"),
                deal = Deal(
                    prices = DealPricesFactory.sample(
                        channelPrices = DealPricesFactory.sampleChannelPrices(
                            ChannelId("getty") to Deal.Prices.Price(
                                BigDecimal.valueOf(2000),
                                Currency.getInstance("USD")
                            ),
                            ChannelId("BBC") to Deal.Prices.Price(BigDecimal.valueOf(2500), Currency.getInstance("USD"))
                        )
                    )
                )
            )


            val prices = priceComputingService.computeVideoOrganisationPrices(
                organisationsPrices = listOf(organisation, organisation2),
                videoId = video.videoId,
                videoTypes = video.types,
                playback = video.playback,
                channel = video.channel.channelId,
            )!!
            assertThat(prices.prices).hasSize(2)
            assertThat(prices.prices?.get(organisation.organisationId)?.amount).isEqualTo(BigDecimal.valueOf(1500))
            assertThat(prices.prices?.get(organisation2.organisationId)?.amount).isEqualTo(BigDecimal.valueOf(2000))
            assertThat(prices.default.amount).isEqualTo(BigDecimal(150))
        }
    }
}

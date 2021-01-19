package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.user.Deal
import com.boclips.videos.service.domain.model.user.OrganisationId
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.testsupport.DealPricesFactory
import com.boclips.videos.service.testsupport.OrganisationFactory
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.boclips.videos.service.testsupport.TestFactories.createYoutubePlayback
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.Currency

class VideoTest {
    @Test
    fun `average returns rating average`() {
        val video = createVideo(
            ratings = listOf(
                UserRating(
                    1,
                    UserId("1")
                ), UserRating(2, UserId("2"))
            )
        )

        assertThat(video.getRatingAverage()).isEqualTo(1.5)
    }

    @Test
    fun `average returns null when empty list`() {
        val video = createVideo(ratings = emptyList())

        assertThat(video.getRatingAverage()).isNull()
    }

    @Test
    fun `is rated by user when no user`() {
        val video = createVideo(
            ratings = listOf(
                UserRating(
                    rating = 3, userId = UserId(
                        "another-teacher"
                    )
                )
            )
        )
        assertThat(video.isRatedByUser(UserId("teacher"))).isFalse()
    }

    @Test
    fun `is rated by user when current user`() {
        val video = createVideo(
            ratings = listOf(
                UserRating(
                    rating = 3, userId = UserId(
                        "teacher"
                    )
                )
            )
        )

        assertThat(video.isRatedByUser(UserId("teacher"))).isTrue()
    }

    @Test
    fun `is rated by user when other user`() {
        val video = createVideo(
            ratings = listOf(
                UserRating(
                    rating = 3, userId = UserId(
                        "anothertheacher"
                    )
                )
            )
        )

        assertThat(video.isRatedByUser(UserId(value = "teacher"))).isFalse()
    }

    @Test
    fun `computes prices based on user's organisation video type prices`() {
        val video = createVideo(types = listOf(VideoType.NEWS))
        val organisationPrices = DealPricesFactory.sample(
            instructional = BigDecimal.valueOf(600),
            news = BigDecimal.ONE,
            stock = BigDecimal.ZERO
        )
        val price = video.getPrice(organisationPrices = organisationPrices)

        assertThat(price!!.amount).isEqualTo(BigDecimal.ONE)
        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
    }

    @Test
    fun `throws when no types are defined on a video when getting price`() {
        val video = createVideo(types = emptyList())
        val organisationPrices = DealPricesFactory.sample(
            instructional = BigDecimal.valueOf(600),
            news = BigDecimal.ONE,
            stock = BigDecimal.ZERO
        )

        assertThrows<VideoMissingTypeException> {
            video.getPrice(organisationPrices = organisationPrices)
        }
    }

    @Test
    fun `does not calculate for non boclips hosted video`() {
        val video = createVideo(playback = createYoutubePlayback(), types = listOf(VideoType.NEWS))
        val organisationPrices = DealPricesFactory.sample(
            instructional = BigDecimal.valueOf(600),
            news = BigDecimal.ONE,
            stock = BigDecimal.ZERO
        )

        val price = video.getPrice(organisationPrices = organisationPrices)
        assertThat(price).isNull()
    }

    @Test
    fun `does not retrieve organisation prices for a non-boclips hosted video`() {
        val video = createVideo(playback = createYoutubePlayback(), types = listOf(VideoType.NEWS))

        val organisation = OrganisationFactory.sample(
            organisationId = OrganisationId("org-id"),
            deal = Deal(prices = DealPricesFactory.sample(stock = BigDecimal.TEN))
        )

        val prices = video.getPrices(listOf(organisation))
        assertThat(prices).isNull()
    }

    @Test
    fun `retrieves organisation prices for a video`() {
        val video = createVideo(types = listOf(VideoType.STOCK))

        val organisation = OrganisationFactory.sample(
            organisationId = OrganisationId("org-id"),
            deal = Deal(prices = DealPricesFactory.sample(stock = BigDecimal.TEN))
        )
        val organisation2 = OrganisationFactory.sample(
            organisationId = OrganisationId("org-id1"),
            deal = Deal(prices = DealPricesFactory.sample(stock = BigDecimal.ONE))
        )

        val prices = video.getPrices(listOf(organisation, organisation2))!!

        assertThat(prices).hasSize(2)
        assertThat(prices[organisation.organisationId]?.amount).isEqualTo(BigDecimal.TEN)
        assertThat(prices[organisation2.organisationId]?.amount).isEqualTo(BigDecimal.ONE)
    }

}

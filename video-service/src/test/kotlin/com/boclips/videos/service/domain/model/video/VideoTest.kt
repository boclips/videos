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

}

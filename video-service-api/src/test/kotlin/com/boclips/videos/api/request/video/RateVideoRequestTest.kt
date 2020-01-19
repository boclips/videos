package com.boclips.videos.api.request.video

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.validation.Validation
import javax.validation.Validator

class RateVideoRequestTest {
    lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.getValidator()
    }

    @Test
    fun `validates rating being null`() {
        val violations = validator.validate(
            RateVideoRequest(
                rating = null,
                videoId = ""
            )
        )

        Assertions.assertThat(violations).hasSize(1)
        Assertions.assertThat(violations.first().message).isEqualTo("Rating is required")
    }

    @Test
    fun `validates minimum rating`() {
        val violations = validator.validate(
            RateVideoRequest(
                rating = -1,
                videoId = ""
            )
        )

        Assertions.assertThat(violations).hasSize(1)
        Assertions.assertThat(violations.first().message).isEqualTo("Rating must be between 1 and 5")
    }

    @Test
    fun `validates maximum rating`() {
        val violations = validator.validate(
            RateVideoRequest(
                rating = 6,
                videoId = ""
            )
        )

        Assertions.assertThat(violations).hasSize(1)
        Assertions.assertThat(violations.first().message).isEqualTo("Rating must be between 1 and 5")
    }
}

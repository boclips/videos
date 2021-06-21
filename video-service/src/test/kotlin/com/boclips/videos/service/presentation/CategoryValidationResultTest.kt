package com.boclips.videos.service.presentation

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class CategoryValidationResultTest {
    @Test
    fun `creates a meaningful error message when invalid`() {
        val error = DataRowsContainErrors(
            errors = listOf(
                MissingVideoId(rowIndex = 4),
                VideoDoesntExist(rowIndex = 2, videoId = "ABC"),
                VideoDoesntExist(rowIndex = 8, videoId = "DEF"),
                InvalidCategoryCode(rowIndex = 6, code = "HI"),
            )
        )

        val message = error.getMessage()
        Assertions.assertThat(message).contains("Rows 6 are missing a video ID")
        Assertions.assertThat(message).contains("Rows 4, 10 contain invalid Video IDs - ABC, DEF")
        Assertions.assertThat(message).contains("Rows 8 contain invalid or unknown category codes")
    }
}

package com.boclips.videos.service.presentation

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class CategoryValidationResultTest {
    @Test
    fun `creates a meaningful error message when invalid`() {
        val error = DataRowsContainErrors(
            errors = listOf(
                MissingVideoId(rowIndex = 4),
                InvalidVideoId(rowIndex = 2, invalidId = "ABC"),
                InvalidVideoId(rowIndex = 8, invalidId = "DEF"),
                InvalidCategoryCode(rowIndex = 6, code = "HI"),
                )
        )

        val message = error.getMessage()
        Assertions.assertThat(message).contains("Rows 5 are missing a video ID")
        Assertions.assertThat(message).contains("Rows 3, 9 contain invalid Video IDs - ABC, DEF")
        Assertions.assertThat(message).contains("Rows 7 contain invalid or unknown category codes")
    }
}

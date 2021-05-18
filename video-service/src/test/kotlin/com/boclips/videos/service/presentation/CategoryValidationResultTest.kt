package com.boclips.videos.service.presentation

import com.boclips.videos.service.presentation.converters.*
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class CategoryValidationResultTest {
    @Test
    fun `creates a meaningful error message when invalid`() {
        val error = CategoriesInvalid(
            errors = listOf(
                InvalidFile,
                MissingVideoId(rowIndex = 4),
                InvalidVideoId(rowIndex = 2, invalidId = "ABC"),
                InvalidVideoId(rowIndex = 8, invalidId = "DEF"),
                InvalidCategoryCode(rowIndex = 6, code = "HI"),
                )
        )

        val message = error.getMessage()
        Assertions.assertThat(message).contains("The file is not a valid CSV format")
        Assertions.assertThat(message).contains("Rows 4 are missing a video ID")
        Assertions.assertThat(message).contains("Rows 2, 8 contain invalid Video IDs - ABC, DEF")
        Assertions.assertThat(message).contains("Rows 6 contain invalid or unknown category codes")
    }
}

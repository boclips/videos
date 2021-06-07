package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.presentation.InvalidCategoryCode
import com.boclips.videos.service.presentation.InvalidVideoId
import com.boclips.videos.service.presentation.MissingVideoId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CategoryMappingValidatorTest {

    @Test
    fun `returns null when mapping valid`() {
        val result = CategoryMappingValidator.validateMapping(
            0,
            RawCategoryMappingMetadata(
                categoryCode = "A",
                videoId = "5c542aba5438cdbcb56de630"
            ),
            listOf("A", "B")
        )
        assertThat(result).isNull()
    }

    @Test
    fun `returns error when invalid category code provided`() {
        val result = CategoryMappingValidator.validateMapping(
            0,
            RawCategoryMappingMetadata(
                categoryCode = "gibberish",
                videoId = "5c542aba5438cdbcb56de630"
            ),
            listOf("A", "B")
        )
        assertThat(result).isEqualTo(InvalidCategoryCode(0, "gibberish"))
    }

    @Test
    fun `returns error when invalid object id provided for video id`() {
        val result = CategoryMappingValidator.validateMapping(
            0,
            RawCategoryMappingMetadata(
                categoryCode = "A",
                videoId = "invalid"
            ),
            listOf("A", "B")
        )
        assertThat(result).isEqualTo(
            InvalidVideoId(
                rowIndex = 0,
                invalidId = "invalid"
            )
        )
    }

    @Test
    fun `returns error when a row is missing video id`() {
        val result = CategoryMappingValidator.validateMapping(
            0,
            RawCategoryMappingMetadata(
                categoryCode = "A",
                videoId = ""
            ),
            listOf("A", "B")
        )

        assertThat(result).isEqualTo(MissingVideoId(rowIndex = 0))
    }
}

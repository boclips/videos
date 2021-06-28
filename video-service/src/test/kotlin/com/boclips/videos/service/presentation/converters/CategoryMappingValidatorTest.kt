package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.presentation.InvalidCategoryCode
import com.boclips.videos.service.presentation.VideoDoesntExist
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
            listOf("A", "B"),
            listOf("5c542aba5438cdbcb56de630")
        )
        assertThat(result).isNull()
    }

    @Test
    fun `returns null when videoId is missing`() {
        val result = CategoryMappingValidator.validateMapping(
            0,
            RawCategoryMappingMetadata(
                categoryCode = "A",
                videoId = ""
            ),
            listOf("A", "B"),
            listOf("")
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
            listOf("A", "B"),
            listOf("5c542aba5438cdbcb56de630")

        )
        assertThat(result).isEqualTo(InvalidCategoryCode(0, "gibberish"))
    }

    @Test
    fun `returns error when video doesn't match videoId in database`() {
        val id1 = "5c542aba5438cdbcb56de630"
        val id2 = "5c542aba5438cdbcb56de631"

        val result = CategoryMappingValidator.validateMapping(
            0,
            RawCategoryMappingMetadata(
                categoryCode = "A",
                videoId = id1
            ),
            listOf("A", "B"),
            listOf(id2)
        )

        assertThat(result).isEqualTo(VideoDoesntExist(rowIndex = 0, videoId = id1))
    }
}

package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.presentation.InvalidCategoryCode
import com.boclips.videos.service.presentation.InvalidPedagogyTags
import com.boclips.videos.service.presentation.VideoDoesntExist
import com.boclips.videos.service.presentation.converters.videoTagging.CategoryMappingValidator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CategoryMappingValidatorTest {

    @Test
    fun `returns null when mapping valid`() {
        val result = CategoryMappingValidator.validateMapping(
            0,
            RawCategoryMappingMetadata(
                categoryCode = "A",
                videoId = "5c542aba5438cdbcb56de630",
                tag = ""
            ),
            listOf("A", "B"),
            listOf("5c542aba5438cdbcb56de630"),
            listOf("")
        )
        assertThat(result).isNull()
    }

    @Test
    fun `returns null when videoId is missing`() {
        val result = CategoryMappingValidator.validateMapping(
            0,
            RawCategoryMappingMetadata(
                categoryCode = "A",
                videoId = "",
                tag = ""
            ),
            listOf("A", "B"),
            listOf(""),
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
                videoId = "5c542aba5438cdbcb56de630",
                tag = ""
            ),
            listOf("A", "B"),
            listOf("5c542aba5438cdbcb56de630"),
            listOf("")
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
                videoId = id1,
                tag = ""
            ),
            listOf("A", "B"),
            listOf(id2),
            listOf("")
        )

        assertThat(result).isEqualTo(VideoDoesntExist(rowIndex = 0, videoId = id1))
    }

    @Test
    fun `returns valid when tags match tags in database`() {
        val id1 = "5c542aba5438cdbcb56de630"

        val result = CategoryMappingValidator.validateMapping(
            0,
            RawCategoryMappingMetadata(
                categoryCode = "A",
                videoId = id1,
                tag = "Other"
            ),
            listOf("A", "B"),
            listOf(id1),
            listOf("Other")
        )

        assertThat(result).isNull()
    }

    @Test
    fun `returns error when tags doesn't match tags in database`() {
        val id1 = "5c542aba5438cdbcb56de630"

        val result = CategoryMappingValidator.validateMapping(
            0,
            RawCategoryMappingMetadata(
                categoryCode = "A",
                videoId = id1,
                tag = "Other"
            ),
            listOf("A", "B"),
            listOf(id1),
            listOf("Hook")
        )

        assertThat(result).isEqualTo(InvalidPedagogyTags(rowIndex = 0, tag = "Other"))
    }
}

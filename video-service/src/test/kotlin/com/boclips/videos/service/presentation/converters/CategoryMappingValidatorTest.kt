package com.boclips.videos.service.presentation.converters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.InputStreamResource

class CategoryMappingValidatorTest {
    @Test
    fun `returns success when valid`() {
        val result = CategoryMappingValidator.validate(fixture("valid-categories.csv"))
        assertThat(result).isEqualTo(Valid(entries = 2))
    }

    @Test
    fun `returns error when invalid category code provided`() {
        val result = CategoryMappingValidator.validate(fixture("categories-invalid-thema-code.csv"))
        assertThat(result).isEqualTo(InvalidCategoryCode(code = "NO CONTENT"))
    }

    @Test
    fun `returns error when invalid object id provided for video id`() {
        val result = CategoryMappingValidator.validate(fixture("categories-invalid-object-id.csv"))
        assertThat(result).isEqualTo(InvalidVideoId(rowIndex = 1, invalidId = "HITHERE"))
    }

    private fun fixture(name: String) =
        InputStreamResource(ClassLoader.getSystemResourceAsStream(name))

    // @Test
    // fun `returns error when a row is missing video id`() {
    //     val input = "Thema code (where possible),Discipline display name (where possible),ID,Link\n" +
    //         "PST,Plants: Living and Nonliving Things,,https://publishers.boclips.com/video/5c54da69d8eafeecae225bbf\n" +
    //         "PSTD,Plants: Living and Nonliving Things,5c54da69d8eafeecae225bbf,https://publishers.boclips.com/video/5c54da69d8eafeecae225bbf\n"
    //     val result = CategoryMappingValidator.validate(input.encodeToByteArray())
    //
    //     assertThat(result).isInstanceOf(CategoryValidationResult.MissingVideoId::class.java)
    //     assertThat(result.isValid).isFalse()
    //     assertThat(result.message).isEqualTo("Row 2 contains an invalid video ID!")
    // }
}

package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.service.taxonomy.CategoryRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource

class CategoryMappingValidatorTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var categoryMappingValidator: CategoryMappingValidator

    @Autowired
    lateinit var categoryRepository: CategoryRepository

    @BeforeEach
    fun setUp() {
        categoryRepository.create(Category(code = CategoryCode("PST"), description = "A Category"))
    }

    @Test
    fun `returns success when valid`() {
        val result = categoryMappingValidator.validate(fixture("valid-categories.csv"))
        assertThat(result).isEqualTo(CategoriesValid(entries = 2))
    }

    @Test
    fun `returns error when invalid category code provided`() {
        val result = categoryMappingValidator.validate(fixture("categories-invalid-thema-code.csv"))
        assertThat(result).isEqualTo(CategoriesInvalid(errors = listOf(InvalidCategoryCode(0, "NO CONTENT"))))
    }

    @Test
    fun `returns error when invalid object id provided for video id`() {
        val result = categoryMappingValidator.validate(fixture("categories-invalid-object-id.csv"))
        assertThat(result).isEqualTo(CategoriesInvalid(errors = listOf(InvalidVideoId(rowIndex = 1, invalidId = "HITHERE"))))
    }

    @Test
    fun `returns error when a row is missing video id`() {
        val result = categoryMappingValidator.validate(fixture("categories-missing-video-id.csv"))

        assertThat(result).isEqualTo(CategoriesInvalid(errors = listOf(MissingVideoId(rowIndex = 1))))
    }

    @Test
    fun `returns error when input is not a valid csv file`() {
        val result = categoryMappingValidator.validate(fixture("not-csv.txt"))

        assertThat(result).isEqualTo(CategoriesInvalid(errors = listOf(InvalidFile)))
    }

    @Test
    fun `creates a meaningful error message`() {
        val error = CategoriesInvalid(errors = )
    }

    private fun fixture(name: String) =
        InputStreamResource(ClassLoader.getSystemResourceAsStream(name))

}

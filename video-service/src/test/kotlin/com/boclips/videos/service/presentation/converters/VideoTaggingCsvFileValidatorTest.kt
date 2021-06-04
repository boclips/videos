package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.service.taxonomy.CategoryRepository
import com.boclips.videos.service.presentation.CategoriesValid
import com.boclips.videos.service.presentation.DataRowsContainErrors
import com.boclips.videos.service.presentation.InvalidCategoryCode
import com.boclips.videos.service.presentation.InvalidVideoId
import com.boclips.videos.service.presentation.MissingVideoId
import com.boclips.videos.service.presentation.NotCsvFile
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource

class VideoTaggingCsvFileValidatorTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoTaggingCsvFileValidator: VideoTaggingCsvFileValidator

    @Autowired
    lateinit var categoryRepository: CategoryRepository

    @BeforeEach
    fun setUp() {
        categoryRepository.create(Category(code = CategoryCode("A"), description = "A Category 'A'"))
        categoryRepository.create(Category(code = CategoryCode("B"), description = "A Category 'B'"))
    }

    @Test
    fun `returns success when valid`() {
        val result = videoTaggingCsvFileValidator.validate(fixture("video_tagging_csvs/valid.csv"))
        assertThat(result).isEqualTo(CategoriesValid(entries = 4))
    }

    @Test
    fun `returns error when invalid category code provided`() {
        val result = videoTaggingCsvFileValidator.validate(fixture("video_tagging_csvs/invalid_category_code.csv"))
        assertThat(result).isEqualTo(DataRowsContainErrors(errors = listOf(InvalidCategoryCode(0, "gibberish"))))
    }

    @Test
    fun `returns error when invalid object id provided for video id`() {
        val result = videoTaggingCsvFileValidator.validate(fixture("video_tagging_csvs/invalid_data.csv"))
        assertThat(result).isEqualTo(
            DataRowsContainErrors(
                errors = listOf(
                    InvalidVideoId(
                        rowIndex = 0,
                        invalidId = "one"
                    )
                )
            )
        )
    }

    @Test
    fun `returns error when a row is missing video id`() {
        val result = videoTaggingCsvFileValidator.validate(fixture("video_tagging_csvs/invalid_no_video_id_value.csv"))

        assertThat(result).isEqualTo(DataRowsContainErrors(errors = listOf(MissingVideoId(rowIndex = 0))))
    }

    @Test
    fun `returns error when input is not a valid csv file`() {
        val result = videoTaggingCsvFileValidator.validate(fixture("video_tagging_csvs/image.csv"))

        assertThat(result).isEqualTo(NotCsvFile)
    }

    @Test
    fun `returns multiple errors when more than one row contains invalid data`() {
        val result = videoTaggingCsvFileValidator.validate(
            fixture("video_tagging_csvs/invalid_multiple_errors_in_different_rows.csv")
        )

        assertThat(result).isEqualTo(
            DataRowsContainErrors(
                errors = listOf(
                    InvalidVideoId(rowIndex = 0, invalidId = "one"),
                    InvalidVideoId(rowIndex = 1, invalidId = "two"),
                    InvalidCategoryCode(rowIndex = 2, code = "INVALID")
                )
            )
        )
    }

    private fun fixture(name: String) =
        InputStreamResource(ClassLoader.getSystemResourceAsStream(name))

}

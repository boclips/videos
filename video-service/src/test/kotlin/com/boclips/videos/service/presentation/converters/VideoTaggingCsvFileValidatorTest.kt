package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.application.GetAllCategories
import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.service.taxonomy.CategoryRepository
import com.boclips.videos.service.presentation.CategoriesValid
import com.boclips.videos.service.presentation.DataRowsContainErrors
import com.boclips.videos.service.presentation.InvalidCategoryCode
import com.boclips.videos.service.presentation.NotCsvFile
import com.boclips.videos.service.presentation.VideoDoesntExist
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.fakes.FakeVideoRepository
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import java.io.File

class VideoTaggingCsvFileValidatorTest : AbstractSpringIntegrationTest() {
    lateinit var videoTaggingCsvFileValidator: VideoTaggingCsvFileValidator

    @Autowired
    lateinit var getAllCategories: GetAllCategories

    @Autowired
    lateinit var categoryRepository: CategoryRepository

    lateinit var videoRepository: FakeVideoRepository

    @BeforeEach
    fun setUp() {
        videoRepository = FakeVideoRepository()
        videoTaggingCsvFileValidator = VideoTaggingCsvFileValidator(getAllCategories, videoRepository)
        categoryRepository.create(Category(code = CategoryCode("A"), description = "A Category 'A'"))
        categoryRepository.create(Category(code = CategoryCode("B"), description = "A Category 'B'"))
    }

    @Test
    fun `returns success when valid`() {
        val result = videoTaggingCsvFileValidator.validate(fixture("video_tagging_csvs/valid.csv"))
        assertThat(result).isInstanceOf(CategoriesValid::class.java)
        assertThat((result as CategoriesValid).entries).hasSize(4)
    }

    @Test
    fun `returns success when the file contains valid untrimmed data`() {
        val result =
            videoTaggingCsvFileValidator.validate(fixture("video_tagging_csvs/valid_with_non_trimmed_values.csv"))
        assertThat(result).isInstanceOf(CategoriesValid::class.java)
        assertThat((result as CategoriesValid).entries).hasSize(4)
    }

    @Test
    fun `returns error when invalid category code provided`() {
        val result = videoTaggingCsvFileValidator.validate(fixture("video_tagging_csvs/invalid_category_code.csv"))
        assertThat(result).isEqualTo(DataRowsContainErrors(errors = listOf(InvalidCategoryCode(0, "gibberish"))))
    }

    @Test
    fun `returns error when input is not a valid csv file`() {
        val result = videoTaggingCsvFileValidator.validate(fixture("video_tagging_csvs/image.csv"))

        assertThat(result).isEqualTo(NotCsvFile)
    }

    @Test
    fun `returns multiple errors when more than one row contains invalid data`() {
        videoRepository.doesntExist(
            listOf(
                "5c542aba5438cdbcb56de911",
            )
        )

        val result = videoTaggingCsvFileValidator.validate(
            fixture("video_tagging_csvs/invalid_multiple_errors_in_different_rows.csv")
        )

        assertThat(result).isEqualTo(
            DataRowsContainErrors(
                errors = listOf(
                    VideoDoesntExist(rowIndex = 0, videoId = "5c542aba5438cdbcb56de911"),
                    InvalidCategoryCode(rowIndex = 1, code = "INVALID"),
                    InvalidCategoryCode(rowIndex = 2, code = "INVALID")
                )
            )
        )
    }

    @Test
    fun `returns success when video ids are confirmed`() {
        val csvName = "valid-ids.csv"
        val csvFile = File(csvName)
        val saveVideo1Id = saveVideo().value
        val saveVideo2Id = saveVideo().value
        val saveVideo3Id = saveVideo().value

        val header = listOf("ID", "Category Code", "three")
        val row1 = listOf(saveVideo1Id, "A", "three")
        val row2 = listOf(saveVideo2Id, "B", "three")
        val row3 = listOf(saveVideo3Id, "A", "three")

        csvWriter().open(csvName) {
            writeRow(header)
            writeRow(row1)
            writeRow(row2)
            writeRow(row3)
        }

        val fixture = InputStreamResource(csvFile.inputStream())

        val result = videoTaggingCsvFileValidator.validate(fixture)
        assertThat(result).isInstanceOf(CategoriesValid::class.java)
        assertThat((result as CategoriesValid).entries).hasSize(3)

        csvFile.delete()
    }

    private fun fixture(name: String) =
        InputStreamResource(ClassLoader.getSystemResourceAsStream(name))
}

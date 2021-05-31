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
import com.boclips.videos.service.presentation.converters.CategoryMappingMetadata

class CategoryMappingValidatorTest {

    @Test
    fun `returns null when mapping valid`() {
        val result = CategoryMappingValidator.validateMapping(
            0,
            CategoryMappingMetadata(
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
            CategoryMappingMetadata(
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
            CategoryMappingMetadata(
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
            CategoryMappingMetadata(
                categoryCode = "A",
                videoId = ""
            ),
            listOf("A", "B")
        )

        assertThat(result).isEqualTo(MissingVideoId(rowIndex = 0))
    }
}

package com.boclips.videos.service.presentation.converters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource

class CategoryMappingValidatorTest {
    @Value("classpath:valid-categories.csv")
    lateinit var validCategoryCsv: Resource

    @Value("classpath:invalid-categories.csv")
    lateinit var invalidCategoryCsv: Resource


    val validInput = "Subject,Unit,Chapter,Topic,Thema code (where possible),Discipline display name (where possible),Subject display name (where possible),Area display name,Video,ID,Link,Video Description,Duration,Publisher,Type,Sub-type (where possible),Pedagogy (where possible),Feedback\n" +
        "Science 1,Plants and Animals,Plants (3),\"\"\"Identify the characteristics of living and nonliving things\",PST,Mathematics and Science,\"Biology, life sciences\",Botany and plant sciences,Plants: Living and Nonliving Things,5c54da69d8eafeecae225bbf,https://publishers.boclips.com/video/5c54da69d8eafeecae225bbf,\"What exactly is a plant? This colorful program explains to students the difference between living and nonliving things as well as what a plant is, where plants live, and the different types of plants they might be familiar with.\",00:01:18,Visual Learning Systems,INSTRUCTIONAL,\"Documentary Short, Narrated\",Explainer,\n" +
        "Science 1,Plants and Animals,Plants (3),\"\"\"Relate plant survival and growth to God’s creational\n" +
        "design \"\"\",,,,,,,,,,,,,,\n" +
        "Science 1,Plants and Animals,Plants (3),\"Investigation:\n" +
        "•        Predict the effects on the growth and survival of a plant when its needs are not met\n" +
        "•        Observe and describe parts of a plant\n" +
        "•        Draw a conclusion about plant needs (about the growth and survival of plants) based on observations\",PST,Mathematics and Science,\"Biology, life sciences\",Botany and plant sciences,Plant Structures: Introduction (Plant Structures),5c54da7ad8eafeecae226402,https://publishers.boclips.com/video/5c54da7ad8eafeecae226402,\"Students will explore the basic parts of a plant including the stem, roots, and leaves. The series of videos describes the functions of each of these plant parts using many real-world examples that students will recognize easily. Important terminology includes: tap root, fibrous root, stem, sap, nutrients, and leaf.\",00:00:58,Visual Learning Systems,INSTRUCTIONAL,\"Documentary Short, Narrated\",Hook,\n"


    @Test
    fun `returns success when valid`() {

        val result = CategoryMappingValidator.validate(validInput.encodeToByteArray())

        assertThat(result).isInstanceOf(CategoryValidationResult.Valid::class.java)
        assertThat(result.isValid).isTrue()
        assertThat(result.message).isEqualTo("Valid CSV, 4 entries parsed")
    }

    @Test
    fun `returns error when invalid category code provided`() {
        val input = "Thema code (where possible),Discipline display name (where possible),ID,Link\n" +
        "PST,Plants: Living and Nonliving Things,5c54da69d8eafeecae225bbf,https://publishers.boclips.com/video/5c54da69d8eafeecae225bbf\n" +
            "PSTD,Plants: Living and Nonliving Things,5c54da69d8eafeecae225bbf,https://publishers.boclips.com/video/5c54da69d8eafeecae225bbf\n"


        val result = CategoryMappingValidator.validate(input.encodeToByteArray())

        assertThat(result).isInstanceOf(CategoryValidationResult.InvalidCategoryCode::class.java)
        assertThat(result.isValid).isFalse()
        assertThat(result.message).isEqualTo("Row 3 contains an invalid video ID!")
    }

    @Test
    fun `returns error when invalid object id provided for video id`() {
        val input = "Thema code (where possible),Discipline display name (where possible),ID,Link\n" +
            "PST,Plants: Living and Nonliving Things,bla,https://publishers.boclips.com/video/5c54da69d8eafeecae225bbf\n" +
            "PSTD,Plants: Living and Nonliving Things,5c54da69d8eafeecae225bbf,https://publishers.boclips.com/video/5c54da69d8eafeecae225bbf\n"
        val result = CategoryMappingValidator.validate(input.encodeToByteArray())

        assertThat(result).isInstanceOf(CategoryValidationResult.InvalidVideoId::class.java)
        assertThat(result.isValid).isFalse()
        assertThat(result.message).isEqualTo("Row 0 contains an invalid video ID!")
    }

    @Test
    fun `returns error when a row is missing video id`() {
        val input = "Thema code (where possible),Discipline display name (where possible),ID,Link\n" +
            "PST,Plants: Living and Nonliving Things,,https://publishers.boclips.com/video/5c54da69d8eafeecae225bbf\n" +
            "PSTD,Plants: Living and Nonliving Things,5c54da69d8eafeecae225bbf,https://publishers.boclips.com/video/5c54da69d8eafeecae225bbf\n"
        val result = CategoryMappingValidator.validate(input.encodeToByteArray())

        assertThat(result).isInstanceOf(CategoryValidationResult.MissingVideoId::class.java)
        assertThat(result.isValid).isFalse()
        assertThat(result.message).isEqualTo("Row 2 contains an invalid video ID!")
    }
}

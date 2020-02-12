package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.EduAgeRange
import com.boclips.contentpartner.service.domain.model.EduAgeRangeId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EduAgeRangeDocumentConverterTest {

    @Test
    fun `round-trips a new age range conversion via document`() {
        val original = EduAgeRange(
            id = EduAgeRangeId("new"),
            label = "this is a label",
            min = 3,
            max = 5
        )

        val document = EduAgeRangeDocumentConverter.toEduAgeRangeDocument(original)
        val convertedAsset = EduAgeRangeDocumentConverter.toEduAgeRange(document)

        assertThat(convertedAsset).isEqualTo(original)
    }
}
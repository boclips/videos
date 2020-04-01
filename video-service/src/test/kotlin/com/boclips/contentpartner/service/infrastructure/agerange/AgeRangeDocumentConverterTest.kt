package com.boclips.contentpartner.service.infrastructure.agerange

import com.boclips.contentpartner.service.domain.model.agerange.AgeRange
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.infrastructure.agerange.AgeRangeDocumentConverter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AgeRangeDocumentConverterTest {

    @Test
    fun `round-trips a new age range conversion via document`() {
        val original = AgeRange(
            id = AgeRangeId("new"),
            label = "this is a label",
            min = 3,
            max = 5
        )

        val document = AgeRangeDocumentConverter.toAgeRangeDocument(original)
        val convertedAsset = AgeRangeDocumentConverter.toAgeRange(document)

        assertThat(convertedAsset).isEqualTo(original)
    }
}

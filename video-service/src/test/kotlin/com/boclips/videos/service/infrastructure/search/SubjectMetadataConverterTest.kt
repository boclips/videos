package com.boclips.videos.service.infrastructure.search

import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.suggestions.SubjectSuggestion
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class SubjectMetadataConverterTest {

    @Test
    fun `should convert to subject metadata` () {
        val suggestion = SubjectSuggestion(
            id = SubjectId("some id"),
            name = "subject name"
        )
        val subjectMetadata = SubjectMetadataConverter.convert(suggestion)

        Assertions.assertThat(subjectMetadata.id).isEqualTo("some id")
        Assertions.assertThat(subjectMetadata.name).isEqualTo("subject name")
    }
}
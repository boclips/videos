package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.videos.service.domain.model.suggestions.SubjectSuggestion

object SubjectMetadataConverter {
    fun convert(subjectSuggestion: SubjectSuggestion): SubjectMetadata {
        return SubjectMetadata(
            id = subjectSuggestion.id.value,
            name = subjectSuggestion.name
        )
    }
}

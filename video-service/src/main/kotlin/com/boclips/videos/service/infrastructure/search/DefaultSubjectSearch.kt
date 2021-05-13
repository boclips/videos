package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.suggestions.SuggestionsIndexReader
import com.boclips.search.service.domain.subjects.SubjectSearchAdapter
import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.videos.service.domain.model.suggestions.SubjectSuggestion
import com.boclips.videos.service.domain.service.suggestions.SubjectIndex

class DefaultSubjectSearch(
    suggestionsIndexReader: SuggestionsIndexReader<SubjectMetadata, SuggestionQuery<SubjectMetadata>>,
    indexWriter: IndexWriter<SubjectMetadata>
) : SubjectSearchAdapter<SubjectSuggestion>(suggestionsIndexReader, indexWriter), SubjectIndex {

    override fun convert(document: SubjectSuggestion): SubjectMetadata {
        return SubjectMetadataConverter.convert(document)
    }
}

package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.suggestions.IndexReader
import com.boclips.search.service.domain.subjects.SubjectSearchAdapter
import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.search.service.domain.subjects.model.SubjectQuery
import com.boclips.videos.service.domain.model.suggestions.SubjectSuggestion
import com.boclips.videos.service.domain.service.suggestions.SubjectIndex

class DefaultSubjectSearch(
    indexReader: IndexReader<SubjectMetadata, SubjectQuery>,
    indexWriter: IndexWriter<SubjectMetadata>
) : SubjectSearchAdapter<SubjectSuggestion>(indexReader, indexWriter), SubjectIndex {

    override fun convert(document: SubjectSuggestion): SubjectMetadata {
        return SubjectMetadataConverter.convert(document)
    }
}

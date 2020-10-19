package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.suggestions.IndexReader
import com.boclips.search.service.domain.common.suggestions.Suggestion
import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.search.service.infrastructure.common.suggestions.AbstractInMemoryFakeSuggestions

class SubjectIndexFake :
    AbstractInMemoryFakeSuggestions<SuggestionQuery<SubjectMetadata>, SubjectMetadata>(),
    IndexReader<SubjectMetadata, SuggestionQuery<SubjectMetadata>>,
    IndexWriter<SubjectMetadata> {
    override fun upsertMetadata(index: MutableMap<String, SubjectMetadata>, item: SubjectMetadata) {
        index[item.id] = item.copy()
    }

    override fun nameMatching(
        index: MutableMap<String, SubjectMetadata>,
        query: SuggestionQuery<SubjectMetadata>
    ): List<Suggestion> {
        val phrase = query.phrase

        return index
            .filter { entry ->
                entry.value.name.contains(phrase, ignoreCase = true)
            }.map {
                Suggestion(
                    name = it.value.name,
                    id = it.value.id
                )
            }
    }
}

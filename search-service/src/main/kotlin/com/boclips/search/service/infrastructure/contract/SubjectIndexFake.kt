package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.common.model.SuggestionRequest
import com.boclips.search.service.domain.common.suggestions.Suggestion
import com.boclips.search.service.domain.common.suggestions.SuggestionsIndexReader
import com.boclips.search.service.domain.search.SearchSuggestionsResults
import com.boclips.search.service.domain.subjects.model.SubjectMetadata

class SubjectIndexFake :
    SuggestionsIndexReader<SubjectMetadata, SuggestionQuery<SubjectMetadata>>,
    IndexWriter<SubjectMetadata> {
    private val fakeSuggestionsIndex = FakeSuggestionsIndex<SuggestionQuery<SubjectMetadata>, SubjectMetadata>()

    override fun safeRebuildIndex(items: Sequence<SubjectMetadata>, notifier: ProgressNotifier?) {
        fakeSuggestionsIndex.safeRebuildIndex(items, this::transformMetadata, notifier)
    }

    override fun upsert(items: Sequence<SubjectMetadata>, notifier: ProgressNotifier?) {
        fakeSuggestionsIndex.upsert(items, this::transformMetadata, notifier)
    }

    override fun removeFromSearch(itemId: String) {
        fakeSuggestionsIndex.removeFromSearch(itemId)
    }

    override fun bulkRemoveFromSearch(itemIds: List<String>) {
        fakeSuggestionsIndex.bulkRemoveFromSearch(itemIds)
    }

    override fun getSuggestions(suggestionRequest: SuggestionRequest<SuggestionQuery<SubjectMetadata>>): SearchSuggestionsResults {
        return fakeSuggestionsIndex.getSuggestions(suggestionRequest, this::nameMatching)
    }

    override fun makeSureIndexIsThere() {
    }

    private fun transformMetadata(item: SubjectMetadata): Pair<String, SubjectMetadata> {
        return Pair(item.id, item.copy())
    }

    private fun nameMatching(
        index: Map<String, SubjectMetadata>,
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

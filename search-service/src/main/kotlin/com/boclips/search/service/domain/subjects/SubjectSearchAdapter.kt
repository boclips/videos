package com.boclips.search.service.domain.subjects

import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.common.model.SearchRequestWithoutPagination
import com.boclips.search.service.domain.common.suggestions.IndexReader
import com.boclips.search.service.domain.search.SearchSuggestionsResults
import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.search.service.domain.subjects.model.SubjectQuery

abstract class SubjectSearchAdapter<T>(
    private val indexReader: IndexReader<SubjectMetadata, SubjectQuery>,
    private val indexWriter: IndexWriter<SubjectMetadata>
) : IndexReader<SubjectMetadata, SubjectQuery>, IndexWriter<T> {
    override fun safeRebuildIndex(items: Sequence<T>, notifier: ProgressNotifier?) {
        indexWriter.safeRebuildIndex(items.map(::convert), notifier)
    }

    override fun upsert(items: Sequence<T>, notifier: ProgressNotifier?) {
        indexWriter.upsert(items.map(::convert), notifier)
    }

    override fun search(searchRequest: SearchRequestWithoutPagination<SubjectQuery>): SearchSuggestionsResults {
        return indexReader.search(searchRequest)
    }

    override fun removeFromSearch(itemId: String) {
        indexWriter.removeFromSearch(itemId)
    }

    override fun bulkRemoveFromSearch(itemIds: List<String>) {
        indexWriter.bulkRemoveFromSearch(itemIds)
    }

    override fun makeSureIndexIsThere() {
        indexWriter.makeSureIndexIsThere()
    }

    abstract fun convert(document: T): SubjectMetadata
}

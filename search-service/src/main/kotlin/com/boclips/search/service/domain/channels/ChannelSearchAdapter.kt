package com.boclips.search.service.domain.channels

import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.common.SearchResults
import com.boclips.search.service.domain.common.model.IndexSearchRequest
import com.boclips.search.service.domain.common.model.SuggestionRequest
import com.boclips.search.service.domain.common.suggestions.SuggestionsIndexReader
import com.boclips.search.service.domain.search.SearchSuggestionsResults

abstract class ChannelSearchAdapter<T>(
    private val suggestionsIndexReader: SuggestionsIndexReader<ChannelMetadata, SuggestionQuery<ChannelMetadata>>,
    private val indexReader: IndexReader<ChannelMetadata, ChannelQuery>,
    private val indexWriter: IndexWriter<ChannelMetadata>
) : SuggestionsIndexReader<ChannelMetadata, SuggestionQuery<ChannelMetadata>>,
    IndexWriter<T>,
    IndexReader<ChannelMetadata, ChannelQuery> {
    override fun safeRebuildIndex(items: Sequence<T>, notifier: ProgressNotifier?) {
        indexWriter.safeRebuildIndex(items.map(::convert), notifier)
    }

    override fun upsert(items: Sequence<T>, notifier: ProgressNotifier?) {
        indexWriter.upsert(items.map(::convert), notifier)
    }

    override fun getSuggestions(suggestionRequest: SuggestionRequest<SuggestionQuery<ChannelMetadata>>): SearchSuggestionsResults {
        return suggestionsIndexReader.getSuggestions(suggestionRequest)
    }

    override fun search(searchRequest: IndexSearchRequest<ChannelQuery>): SearchResults {
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

    abstract fun convert(document: T): ChannelMetadata
}

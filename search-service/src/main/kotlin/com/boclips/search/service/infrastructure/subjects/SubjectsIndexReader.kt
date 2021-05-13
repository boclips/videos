package com.boclips.search.service.infrastructure.subjects

import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.model.SuggestionsSearchRequest
import com.boclips.search.service.domain.search.SearchSuggestionsResults
import com.boclips.search.service.domain.common.suggestions.IndexReader
import com.boclips.search.service.domain.common.suggestions.Suggestion
import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import mu.KLogging
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.builder.SearchSourceBuilder

class SubjectsIndexReader(val client: RestHighLevelClient) : IndexReader<SubjectMetadata, SuggestionQuery<SubjectMetadata>> {
    companion object : KLogging()

    private val elasticSearchResultConverter = SubjectsDocumentConverter()

    override fun search(searchRequest: SuggestionsSearchRequest<SuggestionQuery<SubjectMetadata>>): SearchSuggestionsResults {
        val results = searchQuery(searchRequest.query)

        val elements = results
            .map(elasticSearchResultConverter::convert)
            .map {
                Suggestion(
                    id = it.id,
                    name = it.name
                )
            }

        return SearchSuggestionsResults(elements = elements)
    }

    private fun searchQuery(subjectQuery: SuggestionQuery<SubjectMetadata>): SearchHits {
        val query = SearchSourceBuilder().apply {
            query(SubjectsEsQuery().mainQuery(subjectQuery))
        }

        val request = SearchRequest(
            arrayOf(SubjectsIndex.getIndexAlias()),
            query.from(0).size(10)
        )

        return client.search(request, RequestOptions.DEFAULT).hits
    }
}

package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.channels.ChannelSearchAdapter
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.suggestions.IndexReader
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
import com.boclips.videos.service.domain.service.suggestions.ChannelIndex

class DefaultChannelSearch(
    indexReader: IndexReader<ChannelMetadata, SuggestionQuery<ChannelMetadata>>,
    indexWriter: IndexWriter<ChannelMetadata>
) : ChannelSearchAdapter<ChannelSuggestion>(indexReader, indexWriter), ChannelIndex {

    override fun convert(document: ChannelSuggestion): ChannelMetadata {
        return ChannelMetadataConverter.convert(document)
    }
}
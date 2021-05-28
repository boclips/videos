package com.boclips.videos.service.domain.service.suggestions

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.suggestions.SuggestionsIndexReader

interface ChannelIndex : SuggestionsIndexReader<ChannelMetadata, SuggestionQuery<ChannelMetadata>>,
    IndexWriter<Channel>,
    IndexReader<ChannelMetadata, ChannelQuery>

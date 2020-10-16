package com.boclips.videos.service.domain.service.suggestions

import com.boclips.search.service.domain.common.suggestions.IndexReader
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion

interface ChannelIndex : IndexReader<ChannelMetadata, ChannelQuery>, IndexWriter<ChannelSuggestion>

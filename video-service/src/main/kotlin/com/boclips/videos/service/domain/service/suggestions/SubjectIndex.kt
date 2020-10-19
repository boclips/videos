package com.boclips.videos.service.domain.service.suggestions

import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.suggestions.IndexReader
import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.videos.service.domain.model.suggestions.SubjectSuggestion

interface SubjectIndex : IndexReader<SubjectMetadata, SuggestionQuery<SubjectMetadata>>, IndexWriter<SubjectSuggestion>

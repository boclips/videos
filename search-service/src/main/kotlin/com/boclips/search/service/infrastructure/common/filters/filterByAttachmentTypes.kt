package com.boclips.search.service.infrastructure.common.filters

import com.boclips.search.service.infrastructure.videos.VideoDocument
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders

fun filterByAttachmentTypes(includeTypes: Set<String>): BoolQueryBuilder? {
    val queries = QueryBuilders.boolQuery().queryName(VideoFilterCriteria.ATTACHMENT_TYPES)
    for (r: String in includeTypes) {
        queries.should(QueryBuilders.matchPhraseQuery(VideoDocument.ATTACHMENT_TYPES, r))
    }
    return queries
}
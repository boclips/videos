package com.boclips.search.service.infrastructure.common.filters

import com.boclips.search.service.infrastructure.videos.VideoDocument
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders

fun matchAttachmentTypes(attachmentTypes: Set<String>): BoolQueryBuilder? {
    return QueryBuilders.boolQuery().apply {
        queryName(VideoFilterCriteria.ATTACHMENT_TYPES)
        should(QueryBuilders.termsQuery(VideoDocument.ATTACHMENT_TYPES, attachmentTypes))
    }
}

package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VoiceType
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.elasticsearch.index.query.QueryBuilders.existsQuery
import org.elasticsearch.index.query.QueryBuilders.idsQuery
import org.elasticsearch.index.query.QueryBuilders.termQuery
import org.elasticsearch.index.query.QueryBuilders.termsQuery

class AccessRulesFilter {
    companion object {
        fun buildAccessRulesFilter(boolQueryBuilder: BoolQueryBuilder, videoQuery: VideoQuery): BoolQueryBuilder {
            if (videoQuery.excludedContentPartnerIds.isNotEmpty()) {
                boolQueryBuilder.mustNot(
                    termsQuery(
                        VideoDocument.CONTENT_PARTNER_ID,
                        videoQuery.excludedContentPartnerIds
                    )
                )
            }

            if (videoQuery.includedTypes.isNotEmpty()) {
                boolQueryBuilder.filter(termsQuery(VideoDocument.TYPES, videoQuery.includedTypes))
            }

            if (videoQuery.excludedTypes.isNotEmpty()) {
                boolQueryBuilder.mustNot(termsQuery(VideoDocument.TYPES, videoQuery.excludedTypes))
            }

            if (!videoQuery.deniedVideoIds.isNullOrEmpty()) {
                boolQueryBuilder.mustNot(termsQuery(VideoDocument.ID, videoQuery.deniedVideoIds))
            }

            if (videoQuery.isEligibleForStream != null) {
                boolQueryBuilder.filter(termQuery(VideoDocument.ELIGIBLE_FOR_STREAM, videoQuery.isEligibleForStream))
            }

            val combinedQuery = boolQuery()
            if (!videoQuery.permittedVideoIds.isNullOrEmpty()) {
                combinedQuery.should(idsQuery().addIds(* (videoQuery.permittedVideoIds.toTypedArray())))
            }

            if (!videoQuery.includedChannelIds.isNullOrEmpty()) {
                combinedQuery.should(termsQuery(VideoDocument.CONTENT_PARTNER_ID, videoQuery.includedChannelIds))
            }

            if (combinedQuery.hasClauses()) {
                boolQueryBuilder.filter(combinedQuery)
            }

            if (videoQuery.includedVoiceType.isNotEmpty()) {
                val voicedQuery = boolQuery()
                videoQuery.includedVoiceType.map {
                    when (it) {
                        VoiceType.WITH -> boolQuery().must(termQuery(VideoDocument.IS_VOICED, true))
                        VoiceType.WITHOUT -> boolQuery().must(termQuery(VideoDocument.IS_VOICED, false))
                        VoiceType.UNKNOWN -> boolQuery().mustNot(existsQuery(VideoDocument.IS_VOICED))
                    }
                }.forEach { voicedQuery.should(it) }

               boolQueryBuilder.filter(voicedQuery)
            }

            return boolQueryBuilder
        }
    }
}

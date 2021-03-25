package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VoiceType
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.elasticsearch.index.query.QueryBuilders.existsQuery
import org.elasticsearch.index.query.QueryBuilders.idsQuery
import org.elasticsearch.index.query.QueryBuilders.termQuery
import org.elasticsearch.index.query.QueryBuilders.termsQuery

class AccessRulesFilter {
    companion object {
        fun buildAccessRulesFilter(boolQueryBuilder: BoolQueryBuilder, videoQueryVideo: VideoAccessRuleQuery): BoolQueryBuilder {
            if (videoQueryVideo.excludedContentPartnerIds.isNotEmpty()) {
                boolQueryBuilder.mustNot(
                    termsQuery(
                        VideoDocument.CONTENT_PARTNER_ID,
                        videoQueryVideo.excludedContentPartnerIds
                    )
                )
            }

            if (videoQueryVideo.includedTypes.isNotEmpty()) {
                boolQueryBuilder.filter(termsQuery(VideoDocument.TYPES, videoQueryVideo.includedTypes))
            }

            if (videoQueryVideo.excludedTypes.isNotEmpty()) {
                boolQueryBuilder.mustNot(termsQuery(VideoDocument.TYPES, videoQueryVideo.excludedTypes))
            }

            if (videoQueryVideo.excludedLanguages.isNotEmpty()) {
                boolQueryBuilder.mustNot(termsQuery(VideoDocument.LANGUAGE, videoQueryVideo.excludedLanguages))
            }

            if (!videoQueryVideo.deniedVideoIds.isNullOrEmpty()) {
                boolQueryBuilder.mustNot(termsQuery(VideoDocument.ID, videoQueryVideo.deniedVideoIds))
            }

            if (videoQueryVideo.isEligibleForStream != null) {
                boolQueryBuilder.filter(termQuery(VideoDocument.ELIGIBLE_FOR_STREAM, videoQueryVideo.isEligibleForStream))
            }

            if (videoQueryVideo.isEligibleForDownload != null) {
                boolQueryBuilder.filter(termQuery(VideoDocument.ELIGIBLE_FOR_DOWNLOAD, videoQueryVideo.isEligibleForDownload))
            }

            val combinedQuery = boolQuery()
            if (!videoQueryVideo.permittedVideoIds.isNullOrEmpty()) {
                combinedQuery.should(idsQuery().addIds(* (videoQueryVideo.permittedVideoIds.toTypedArray())))
            }

            if (!videoQueryVideo.includedChannelIds.isNullOrEmpty()) {
                combinedQuery.should(termsQuery(VideoDocument.CONTENT_PARTNER_ID, videoQueryVideo.includedChannelIds))
            }

            if (combinedQuery.hasClauses()) {
                boolQueryBuilder.filter(combinedQuery)
            }

            if (videoQueryVideo.includedVoiceType.isNotEmpty()) {
                val voicedQuery = boolQuery()
                videoQueryVideo.includedVoiceType.map {
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

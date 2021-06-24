package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.ChannelAccessRuleQuery
import com.boclips.search.service.infrastructure.videos.VideoDocument
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.QueryBuilders.termsQuery

class ChannelsAccessRulesFilter {
    companion object {
        fun channelsBuildAccessRulesFilter(
            boolQueryBuilder: BoolQueryBuilder,
            accessRulesQuery: ChannelAccessRuleQuery
        ): BoolQueryBuilder {

            if (accessRulesQuery.excludedContentPartnerIds.isNotEmpty()) {
                boolQueryBuilder.mustNot(
                    termsQuery(
                        ChannelDocument.ID,
                        accessRulesQuery.excludedContentPartnerIds
                    )
                )
            }

            if (accessRulesQuery.includedTypes.isNotEmpty()) {
                boolQueryBuilder.filter(termsQuery(ChannelDocument.TYPES, accessRulesQuery.includedTypes))
            }

            if (accessRulesQuery.excludedTypes.isNotEmpty()) {
                boolQueryBuilder.mustNot(termsQuery(ChannelDocument.TYPES, accessRulesQuery.excludedTypes))
            }

            if (accessRulesQuery.isEligibleForStream != null) {
                boolQueryBuilder.filter(
                    QueryBuilders.termQuery(
                        VideoDocument.ELIGIBLE_FOR_STREAM,
                        accessRulesQuery.isEligibleForStream
                    )
                )
            }

            if (!accessRulesQuery.includedChannelIds.isNullOrEmpty()) {
                boolQueryBuilder.should(termsQuery(ChannelDocument.ID, accessRulesQuery.includedChannelIds))
            }

            if (accessRulesQuery.includedPrivateChannelIds.isNotEmpty()) {
                boolQueryBuilder.must(
                    QueryBuilders.boolQuery()
                        .should(termsQuery(ChannelDocument.ID, accessRulesQuery.includedPrivateChannelIds))
                        .should(
                            termsQuery(
                                ChannelDocument.IS_PRIVATE,
                                false
                            )
                        )
                )
            } else {
                boolQueryBuilder.must(
                    termsQuery(
                        ChannelDocument.IS_PRIVATE,
                        false
                    )
                )
            }

            return boolQueryBuilder
        }
    }
}

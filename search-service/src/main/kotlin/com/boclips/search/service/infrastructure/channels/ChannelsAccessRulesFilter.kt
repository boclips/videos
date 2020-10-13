package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.videos.model.AccessRuleQuery
import com.boclips.search.service.infrastructure.videos.VideoDocument
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.termsQuery

class ChannelsAccessRulesFilter {
    companion object {
        fun channelsBuildAccessRulesFilter(boolQueryBuilder: BoolQueryBuilder, accessRulesQuery: AccessRuleQuery): BoolQueryBuilder {

            if (accessRulesQuery.excludedContentPartnerIds.isNotEmpty()) {
                boolQueryBuilder.mustNot(
                    termsQuery(
                        VideoDocument.ID,
                        accessRulesQuery.excludedContentPartnerIds
                    )
                )
            }

            if (!accessRulesQuery.includedChannelIds.isNullOrEmpty()) {
                boolQueryBuilder.should(termsQuery(ChannelDocument.ID, accessRulesQuery.includedChannelIds))
            }

            return boolQueryBuilder
        }
    }
}
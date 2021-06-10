package com.boclips.videos.service.application.accessrules

import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.channel.ChannelId

interface AccessRulesConverter {
    fun toVideoAccess(
        accessRules: List<AccessRuleResource>,
        privateChannels: Set<ChannelId>
    ): VideoAccess

    fun toCollectionAccess(
        accessRules: List<AccessRuleResource>,
        user: User? = null
    ): CollectionAccessRule
}

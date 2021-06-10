package com.boclips.videos.service.domain.service.user

import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.channel.ChannelId

interface AccessRuleService {
    fun getRules(user: User, client: String? = null): AccessRules
    fun getPrivateChannels(): Set<ChannelId>
}

package com.boclips.videos.service.domain.service.user

import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.contentpackage.ContentPackageId
import com.boclips.videos.service.domain.model.video.channel.ChannelId

interface ContentPackageService {
    fun getAccessRules(id: ContentPackageId, hiddenChannels: List<ChannelId>): AccessRules?
}

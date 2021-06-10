package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.channel.ChannelId

data class AccessRules(
    val collectionAccess: CollectionAccessRule,
    val videoAccess: VideoAccess,
) {
    companion object {
        fun anonymousAccess(privateChannels: Set<ChannelId>): AccessRules {
            return AccessRules(
                videoAccess = VideoAccess.Everything(privateChannels = privateChannels),
                collectionAccess = CollectionAccessRule.Everything
            )
        }
    }
}

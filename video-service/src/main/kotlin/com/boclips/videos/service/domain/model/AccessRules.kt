package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.video.VideoAccess

data class AccessRules(val collectionAccess: CollectionAccessRule, val videoAccess: VideoAccess) {
    companion object {
        fun anonymousAccess(): AccessRules {
            return AccessRules(videoAccess = VideoAccess.Everything, collectionAccess = CollectionAccessRule.Everything)
        }
    }
}

package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.video.VideoAccessRule

data class AccessRules(val collectionAccess: CollectionAccessRule, val videoAccess: VideoAccessRule)

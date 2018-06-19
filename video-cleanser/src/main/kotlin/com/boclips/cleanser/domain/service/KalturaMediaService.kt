package com.boclips.cleanser.domain.service

import com.boclips.cleanser.domain.model.MediaItem

interface KalturaMediaService {
    fun getReadyMediaEntries(): Set<MediaItem>
}
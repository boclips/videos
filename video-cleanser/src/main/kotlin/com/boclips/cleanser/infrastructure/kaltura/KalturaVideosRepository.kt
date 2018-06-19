package com.boclips.cleanser.infrastructure.kaltura

import com.boclips.cleanser.infrastructure.kaltura.response.MediaItem
import org.springframework.stereotype.Component

@Component
class KalturaVideosRepository(val kalturaMediaClient: KalturaMediaClient) {
    fun getReadyMediaEntries(): Set<MediaItem> {
        return kalturaMediaClient
                .fetch(filters = listOf(MediaFilter(MediaFilterType.STATUS_IN, "2")))
                .toSet()
    }
}

package com.boclips.cleanser.infrastructure.kaltura

import com.boclips.cleanser.domain.model.MediaFilter
import com.boclips.cleanser.domain.model.MediaFilterType
import com.boclips.cleanser.domain.model.MediaItem
import com.boclips.cleanser.domain.service.KalturaMediaService
import com.boclips.cleanser.infrastructure.kaltura.client.KalturaMediaClient
import org.springframework.stereotype.Component

@Component
class PagedKalturaMediaService(val kalturaMediaClient: KalturaMediaClient) : KalturaMediaService {
    override fun getReadyMediaEntries(): Set<MediaItem> {
        return kalturaMediaClient
                .fetch(filters = listOf(MediaFilter(MediaFilterType.STATUS_IN, "2")))
                .toSet()
    }
}

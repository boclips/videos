package com.boclips.cleanser.infrastructure.kaltura

import org.springframework.stereotype.Component

@Component
class KalturaVideosRepository(val kalturaMediaClient: KalturaMediaClient) {
    fun getAllIds(): Set<String> {
        return kalturaMediaClient.fetch(500, 0)
                .map { it.referenceId }.toSet()
    }
}

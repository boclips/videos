package com.boclips.cleanser.infrastructure.kaltura

import org.springframework.stereotype.Component

@Component
class KalturaVideosRepository(val kalturaClient: KalturaClient) {
    fun getAllIds(): Set<String> {
        return kalturaClient.fetchPagedMedia(500, 0)
                .map { it.referenceId }.toSet()
    }
}

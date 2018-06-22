package com.boclips.cleanser.domain.service

import com.boclips.cleanser.domain.model.KalturaVideo

interface KalturaMediaService {
    fun getReadyMediaEntries(): Set<KalturaVideo>
    fun getFaultyMediaEntries(): Set<KalturaVideo>
    fun getPendingMediaEntries(): Set<KalturaVideo>
    fun countAllMediaEntries(): Long
}
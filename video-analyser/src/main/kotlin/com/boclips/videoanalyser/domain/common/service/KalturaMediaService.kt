package com.boclips.videoanalyser.domain.common.service

import com.boclips.videoanalyser.domain.common.model.KalturaVideo

interface KalturaMediaService {
    fun getReadyMediaEntries(): Set<KalturaVideo>
    fun getFaultyMediaEntries(): Set<KalturaVideo>
    fun getPendingMediaEntries(): Set<KalturaVideo>
    fun countAllMediaEntries(): Long
}
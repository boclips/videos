package com.boclips.videoanalyser.domain.service

import com.boclips.videoanalyser.domain.model.KalturaVideo

interface KalturaMediaService {
    fun getReadyMediaEntries(): Set<KalturaVideo>
    fun getFaultyMediaEntries(): Set<KalturaVideo>
    fun getPendingMediaEntries(): Set<KalturaVideo>
    fun countAllMediaEntries(): Long
    fun removeMediaEntries(videos: Set<KalturaVideo>): Set<KalturaVideo>
}
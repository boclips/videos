package com.boclips.cleanser.domain.service

interface KalturaMediaService {
    fun getReadyMediaEntries(): Set<String>
    fun getFaultyMediaEntries(): Set<String>
    fun countAllMediaEntries(): Long
}
package com.boclips.cleanser.domain.service

interface BoclipsVideoService {
    fun getAllPublishedVideos(): Set<String>
}
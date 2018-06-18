package com.boclips.cleanser.domain

interface CleanserService {
    fun getNonPlayableVideos(): Set<String>
}
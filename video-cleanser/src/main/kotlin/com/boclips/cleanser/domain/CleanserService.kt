package com.boclips.cleanser.domain

interface CleanserService {
    fun getUnplayableVideos(): Set<String>
}
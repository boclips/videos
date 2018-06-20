package com.boclips.cleanser.domain.service

interface BoclipsVideoService {
    fun countAllVideos(): Int
    fun getAllVideos(): Set<String>
}
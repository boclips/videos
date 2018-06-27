package com.boclips.videoanalyser.domain.service

import com.boclips.videoanalyser.domain.model.BoclipsVideo

interface BoclipsVideoService {
    fun countAllVideos(): Int
    fun getAllVideos(): Set<BoclipsVideo>
    fun getVideoMetadata(ids: Collection<String>) : Set<BoclipsVideo>
}
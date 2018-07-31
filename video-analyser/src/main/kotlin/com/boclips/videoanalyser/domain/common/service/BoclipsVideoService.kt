package com.boclips.videoanalyser.domain.common.service

import com.boclips.videoanalyser.domain.common.model.BoclipsVideo

interface BoclipsVideoService {
    fun countAllVideos(): Int
    fun getAllVideos(): Set<BoclipsVideo>
    fun getVideoMetadata(ids: Collection<String>) : Set<BoclipsVideo>
    fun deleteVideos(videos : Set<BoclipsVideo>)
}
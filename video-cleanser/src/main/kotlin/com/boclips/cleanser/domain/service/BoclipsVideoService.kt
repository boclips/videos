package com.boclips.cleanser.domain.service

import com.boclips.cleanser.domain.model.BoclipsVideo

interface BoclipsVideoService {
    fun countAllVideos(): Int
    fun getAllVideos(): Set<BoclipsVideo>
}
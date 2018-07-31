package com.boclips.videoanalyser.domain.duplicates.service

import com.boclips.videoanalyser.domain.duplicates.model.Duplicate
import com.boclips.videoanalyser.domain.model.BoclipsVideo

interface DuplicateStrategy {

    fun findDuplicates(videos: Iterable<BoclipsVideo>): Set<Duplicate>

}
package com.boclips.videoanalyser.domain.duplicates.service.strategies

import com.boclips.videoanalyser.domain.common.model.BoclipsVideo
import com.boclips.videoanalyser.domain.duplicates.model.Duplicate

interface DuplicateStrategy {
    fun findDuplicates(videos: Iterable<BoclipsVideo>): Set<Duplicate>
}


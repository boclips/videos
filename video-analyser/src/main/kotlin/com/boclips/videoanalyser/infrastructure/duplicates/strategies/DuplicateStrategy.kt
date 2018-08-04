package com.boclips.videoanalyser.infrastructure.duplicates.strategies

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.domain.model.DuplicateVideo

interface DuplicateStrategy {
    fun findDuplicates(videos: Iterable<BoclipsVideo>): Set<DuplicateVideo>
}


package com.boclips.videoanalyser.infrastructure.duplicates.strategies

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.domain.model.DuplicateVideo

/**
 * Detects duplicates grouping using a supplied key given a specific asset
 */
abstract class AbstractGroupingDuplicateStrategy : DuplicateStrategy {

    abstract fun getGroupingKey(video: BoclipsVideo): Any
    open fun prefilter(video: BoclipsVideo) = true

    final override fun findDuplicates(videos: Iterable<BoclipsVideo>) = videos
            .filter { prefilter(it) }
            .groupBy { video -> getGroupingKey(video) }
            .filter { it.value.size > 1 }
            .map {
                DuplicateVideo(
                        originalVideo = it.value.first(),
                        duplicates = it.value.subList(1, it.value.size))
            }
            .toSet()

}
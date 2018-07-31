package com.boclips.videoanalyser.domain.duplicates.service.strategies

import com.boclips.videoanalyser.domain.common.model.BoclipsVideo
import com.boclips.videoanalyser.domain.duplicates.model.Duplicate

/**
 * Detects duplicates grouping using a supplied key given a specific video
 */
abstract class AbstractGroupingDuplicateStrategy : DuplicateStrategy {

    abstract fun getGroupingKey(video: BoclipsVideo) : Any

    final override fun findDuplicates(videos: Iterable<BoclipsVideo>) = videos
            .groupBy{video -> getGroupingKey(video)}
            .filter { it.value.size > 1 }
            .map {
                Duplicate(
                        originalVideo = it.value.first(),
                        duplicates = it.value.subList(1, it.value.size))
            }
            .toSet()

}
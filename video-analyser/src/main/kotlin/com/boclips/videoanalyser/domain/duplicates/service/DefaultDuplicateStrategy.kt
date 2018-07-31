package com.boclips.videoanalyser.domain.duplicates.service

import com.boclips.videoanalyser.domain.duplicates.model.Duplicate
import com.boclips.videoanalyser.domain.model.BoclipsVideo
import org.springframework.stereotype.Component

@Component
class DefaultDuplicateStrategy : DuplicateStrategy {

    override fun findDuplicates(videos: Iterable<BoclipsVideo>) = videos
            .groupBy { video -> video.contentProvider to video.contentProviderId }
            .filter { it.value.size > 1 }
            .map { Duplicate(
                    originalVideo = it.value.first(),
                    duplicates = it.value.subList(1, it.value.size))
            }
            .toSet()

}
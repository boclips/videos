package com.boclips.videoanalyser.infrastructure.duplicates.strategies

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import org.springframework.stereotype.Component

@Component
class DefaultDuplicateStrategy : AbstractGroupingDuplicateStrategy() {

    override fun getGroupingKey(video: BoclipsVideo) =
            video.contentProvider to video.contentProviderId


}
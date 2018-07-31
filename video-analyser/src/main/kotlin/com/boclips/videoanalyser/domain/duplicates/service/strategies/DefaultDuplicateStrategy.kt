package com.boclips.videoanalyser.domain.duplicates.service.strategies

import com.boclips.videoanalyser.domain.common.model.BoclipsVideo
import org.springframework.stereotype.Component

@Component
class DefaultDuplicateStrategy : AbstractGroupingDuplicateStrategy() {

    override fun getGroupingKey(video: BoclipsVideo) =
            video.contentProvider to video.contentProviderId


}
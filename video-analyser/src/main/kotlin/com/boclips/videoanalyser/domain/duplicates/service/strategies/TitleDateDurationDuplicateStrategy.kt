package com.boclips.videoanalyser.domain.duplicates.service.strategies

import com.boclips.videoanalyser.domain.common.model.BoclipsVideo
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TitleDateDurationDuplicateStrategy : AbstractGroupingDuplicateStrategy() {
    override fun getGroupingKey(video: BoclipsVideo) =
            Key(
                    title = video.title,
                    date = video.date,
                    duration = video.duration,
                    contentProvider = video.contentProvider
            )


    data class Key(
            val title: String?,
            val contentProvider: String?,
            val duration: String?,
            val date: LocalDateTime?
    )
}


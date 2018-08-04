package com.boclips.videoanalyser.infrastructure.duplicates.strategies

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TitleDateDurationDuplicateStrategy : AbstractGroupingDuplicateStrategy() {

    override fun prefilter(video: BoclipsVideo): Boolean {
        return !setOf(
                "1 Minute in the Museum",
                "Atmosphaeres",
                "Bridgeman",
                "EngVid",
                "Intelecom Learning"
        ).contains(video.contentProvider)
    }

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


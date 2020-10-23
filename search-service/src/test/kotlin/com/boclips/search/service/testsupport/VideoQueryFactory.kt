package com.boclips.search.service.testsupport

import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import java.time.Duration
import java.time.LocalDate

class VideoQueryFactory {
    companion object {
        fun aRandomExample(): VideoQuery {
            return VideoQuery(
                phrase = "some phrase",
                userQuery = UserQuery(
                    ageRangeMax = 10,
                    ageRangeMin = 5,
                    subjectIds = setOf("subject-123"),
                    durationRanges = listOf(
                        DurationRange(min = Duration.ofSeconds(20), max = Duration.ofSeconds(100)),
                        DurationRange(min = Duration.ofMinutes(10), max = Duration.ofMinutes(15))
                    ),
                    source = SourceType.BOCLIPS,
                    releaseDateFrom = LocalDate.of(2014, 1, 30),
                    releaseDateTo = LocalDate.of(2015, 1, 30),
                    promoted = true,
                    attachmentTypes = setOf("ACTIVITY"),
                    channelIds = setOf("Achannel-ID11-1111-adc1-0242ac120002", "Achannel-ID22-2222-adc1-0242ac120002"),
                    channelNames = setOf("a channel name")
                ),
                videoAccessRuleQuery = VideoAccessRuleQuery(
                    includedTypes = setOf(VideoType.INSTRUCTIONAL, VideoType.STOCK),
                    excludedTypes = setOf(VideoType.INSTRUCTIONAL)
                )
            )
        }

        fun empty(): VideoQuery {
            return VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery())
        }
    }
}

package com.boclips.videoanalyser.testsupport

import com.boclips.videoanalyser.domain.common.model.BoclipsVideo
import com.boclips.videoanalyser.domain.common.model.KalturaVideo
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

class TestFactory {
    companion object {
        fun boclipsVideo(
                id: Int = Math.random().toInt(),
                contentProviderId: String? = null,
                contentProvider: String? = "Bloomie",
                title: String? = "Some great title",
                duration: String? = null,
                date: LocalDateTime? = null
        ): BoclipsVideo {
            return BoclipsVideo(
                    id = id,
                    title = title,
                    contentProvider = contentProvider,
                    contentProviderId = contentProviderId,
                    duration = duration,
                    date = date
            )
        }

        fun kalturaVideo(referenceId: String): KalturaVideo {
            return KalturaVideo(referenceId = referenceId, id = "89")
        }
    }
}
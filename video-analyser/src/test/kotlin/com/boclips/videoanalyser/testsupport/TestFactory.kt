package com.boclips.videoanalyser.testsupport

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.domain.model.KalturaVideo
import java.time.LocalDateTime

class TestFactory {
    companion object {
        fun boclipsVideo(
                id: Int = Math.random().toInt(),
                referenceId: String? = null,
                contentProviderId: String? = null,
                contentProvider: String? = "Bloomie",
                title: String? = "Some great title",
                duration: String? = null,
                date: LocalDateTime? = null
        ): BoclipsVideo {
            return BoclipsVideo(
                    id = id,
                    referenceId = referenceId ?: "r$id",
                    title = title,
                    contentProvider = contentProvider,
                    contentProviderId = contentProviderId,
                    duration = duration,
                    date = date
            )
        }

        fun kalturaVideo(referenceId: String): KalturaVideo {
            return KalturaVideo(referenceId = referenceId, id = "89", downloadUrl = "http://download.com/1")
        }
    }
}
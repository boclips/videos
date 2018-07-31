package com.boclips.videoanalyser.testsupport

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.domain.model.KalturaVideo
import java.util.*

class TestFactory {
    companion object {
        fun boclipsVideo(
                id: String = UUID.randomUUID().toString(),
                contentProviderId: String? = null,
                contentProvider: String? = "Bloomie"
        ): BoclipsVideo {
            return BoclipsVideo(
                    id = id,
                    title = "Some great title",
                    contentProvider = contentProvider,
                    contentProviderId = contentProviderId
            )
        }

        fun kalturaVideo(referenceId: String): KalturaVideo {
            return KalturaVideo(referenceId = referenceId, id = "89")
        }
    }
}
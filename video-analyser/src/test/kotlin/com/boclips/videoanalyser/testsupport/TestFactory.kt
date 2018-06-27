package com.boclips.videoanalyser.testsupport

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.domain.model.KalturaVideo
import java.util.*

class TestFactory {
    companion object {
        fun boclipsVideo(id: String = UUID.randomUUID().toString()): BoclipsVideo {
            return BoclipsVideo(id = id, title = "Some great title", contentProvider = "Bloomie")
        }

        fun kalturaVideo(referenceId: String): KalturaVideo {
            return KalturaVideo(referenceId = referenceId, id = "89")
        }
    }
}
package com.boclips.testsupport

import com.boclips.cleanser.domain.model.BoclipsVideo
import java.util.*

class TestFactory {
    companion object {
        fun boclipsVideo(id: String = UUID.randomUUID().toString()): BoclipsVideo {
            return BoclipsVideo(id = id, title = "Some great title", contentProvider = "Bloomie")
        }

    }
}
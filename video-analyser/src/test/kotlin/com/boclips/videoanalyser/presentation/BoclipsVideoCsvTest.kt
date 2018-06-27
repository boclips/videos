package com.boclips.videoanalyser.presentation

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.domain.model.KalturaVideo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime
import java.time.Month

class BoclipsVideoCsvTest {

    @Test
    fun fromBoclipsVideo() {
        val expectedCsv = BoclipsVideoCsv(
                id = "1",
                referenceId = "ref-1",
                title = "t1",
                description = "d1",
                provider = "p1",
                duration = "01:02:03",
                date = LocalDateTime.of(2018, Month.DECEMBER, 1, 2, 3, 4)
        )
        val boclipsVideoCsv = BoclipsVideoCsv.from(BoclipsVideo(
                id = "1",
                referenceId = "ref-1",
                title = "t1",
                description = "d1",
                contentProvider = "p1",
                duration = "01:02:03",
                date = LocalDateTime.of(2018, Month.DECEMBER, 1, 2, 3, 4)
        ))

        assertThat(boclipsVideoCsv).isEqualTo(expectedCsv)
    }

    @Test
    fun fromKalturaVideo() {
        val expectedCsv = BoclipsVideoCsv(
                id = "1",
                referenceId = "ref-1",
                title = null,
                description = null,
                provider = null,
                duration = null,
                date = null
        )
        val boclipsVideoCsv = BoclipsVideoCsv.from(KalturaVideo(
                id = "1",
                referenceId = "ref-1"
        ))

        assertThat(boclipsVideoCsv).isEqualTo(expectedCsv)
    }
}
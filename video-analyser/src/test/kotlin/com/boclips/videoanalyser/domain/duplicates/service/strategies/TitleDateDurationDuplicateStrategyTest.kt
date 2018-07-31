package com.boclips.videoanalyser.domain.duplicates.service.strategies

import com.boclips.videoanalyser.domain.duplicates.model.Duplicate
import com.boclips.videoanalyser.domain.duplicates.service.strategies.TitleDateDurationDuplicateStrategy
import com.boclips.videoanalyser.testsupport.TestFactory
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.LocalDateTime

class TitleDateDurationDuplicateStrategyTest {

    val subject = TitleDateDurationDuplicateStrategy()

    @Test
    fun `WHEN same title, CP, date and duration SHOULD be grouped as duplicates`() {
        val now = LocalDateTime.now()
        val originalVideo = TestFactory.boclipsVideo(title = "duplicate", contentProvider = "cp1", date = now, duration = "10:00")
        val duplicate = TestFactory.boclipsVideo(title = "duplicate", contentProvider = "cp1", date = now, duration = "10:00")

        val videos = listOf(
                originalVideo,
                duplicate,
                duplicate
        )

        val output: Set<Duplicate> = subject.findDuplicates(videos)

        Assertions.assertThat(output).containsExactlyInAnyOrder(
                Duplicate(originalVideo = originalVideo, duplicates = listOf(duplicate, duplicate))
        )
    }

    @Test
    fun `WHEN no duplicates`() {
        val now = LocalDateTime.now()
        val originalVideo = TestFactory.boclipsVideo(title = "duplicate", contentProvider = "cp1", date = now, duration = "10:00")
        val videos = listOf(
                originalVideo,

                originalVideo.copy(title="not-duplicate"),
                originalVideo.copy(contentProvider = "cp2"),
                originalVideo.copy(date= LocalDateTime.MIN),
                originalVideo.copy(duration="10:10")
        )

        val output: Set<Duplicate> = subject.findDuplicates(videos)

        Assertions.assertThat(output).isEmpty()
    }
}
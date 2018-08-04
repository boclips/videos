package com.boclips.videoanalyser.infrastructure.duplicates.strategies

import com.boclips.videoanalyser.domain.model.DuplicateVideo
import com.boclips.videoanalyser.infrastructure.duplicates.strategies.TitleDateDurationDuplicateStrategy
import com.boclips.videoanalyser.testsupport.TestFactory
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.LocalDateTime

class TitleDateDurationDuplicateVideoStrategyTest {

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

        val output: Set<DuplicateVideo> = subject.findDuplicates(videos)

        Assertions.assertThat(output).containsExactlyInAnyOrder(
                DuplicateVideo(originalVideo = originalVideo, duplicates = listOf(duplicate, duplicate))
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

        val output: Set<DuplicateVideo> = subject.findDuplicates(videos)

        Assertions.assertThat(output).isEmpty()
    }

    @Test
    fun `WHEN blacklisted`() {
        val now = LocalDateTime.now()
        val originalVideo = TestFactory.boclipsVideo(title = "duplicate", contentProvider = "cp1", date = now, duration = "10:00")
        val videos = listOf(
                originalVideo.copy(contentProvider = "1 Minute in the Museum"),
                originalVideo.copy(contentProvider = "Atmosphaeres"),
                originalVideo.copy(contentProvider = "Bridgeman"),
                originalVideo.copy(contentProvider = "EngVid"),
                originalVideo.copy(contentProvider = "Intelecom Learning"),

                originalVideo.copy(contentProvider = "1 Minute in the Museum"),
                originalVideo.copy(contentProvider = "Atmosphaeres"),
                originalVideo.copy(contentProvider = "Bridgeman"),
                originalVideo.copy(contentProvider = "EngVid"),
                originalVideo.copy(contentProvider = "Intelecom Learning")
        )

        val output: Set<DuplicateVideo> = subject.findDuplicates(videos)

        Assertions.assertThat(output).hasSize(0)
    }
}
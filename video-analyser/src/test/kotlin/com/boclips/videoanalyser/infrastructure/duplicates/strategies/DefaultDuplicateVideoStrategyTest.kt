package com.boclips.videoanalyser.infrastructure.duplicates.strategies

import com.boclips.videoanalyser.domain.model.DuplicateVideo
import com.boclips.videoanalyser.infrastructure.duplicates.strategies.DefaultDuplicateStrategy
import com.boclips.videoanalyser.testsupport.TestFactory.Companion.boclipsVideo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DefaultDuplicateVideoStrategyTest {

    val subject = DefaultDuplicateStrategy()

    @Test
    fun `WHEN videos with same provider & provider id SHOULD be grouped as duplicates`() {
        val originalVideo = boclipsVideo(contentProviderId = "1", contentProvider = "cp1")
        val duplicate1 = boclipsVideo(contentProviderId = "1", contentProvider = "cp1")
        val duplicate2 = boclipsVideo(contentProviderId = "1", contentProvider = "cp1")
        val videos = listOf(
                originalVideo,
                duplicate1,
                boclipsVideo(contentProviderId = "2", contentProvider = "cp1"),
                boclipsVideo(contentProviderId = "1", contentProvider = "cp2"),
                duplicate2
        )

        val output: Set<DuplicateVideo> = subject.findDuplicates(videos)

        assertThat(output).containsExactlyInAnyOrder(
                DuplicateVideo(originalVideo = originalVideo, duplicates = listOf(duplicate1, duplicate2))
        )
    }

    @Test
    fun `WHEN no duplicates`() {
        val videos = listOf(
                boclipsVideo(contentProviderId = "1", contentProvider = "cp1"),
                boclipsVideo(contentProviderId = "2", contentProvider = "cp1"),
                boclipsVideo(contentProviderId = "1", contentProvider = "cp2"),
                boclipsVideo(contentProviderId = "2", contentProvider = "cp2")
        )

        val output: Set<DuplicateVideo> = subject.findDuplicates(videos)

        assertThat(output).isEmpty()
    }
}
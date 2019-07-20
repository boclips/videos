package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideosExclusionFromDownloadRequested
import com.boclips.eventbus.events.video.VideosExclusionFromStreamRequested
import com.boclips.eventbus.events.video.VideosInclusionInDownloadRequested
import com.boclips.eventbus.events.video.VideosInclusionInStreamRequested
import com.boclips.videos.service.application.video.exceptions.InvalidBulkUpdateRequestException
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class BulkUpdateVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Nested
    inner class UsingResourceDistributionMethods {
        @Test
        fun `can disable from all delivery methods`() {
            val videoIds = listOf(saveVideo(), saveVideo())

            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = videoIds.map { it.value },
                    distributionMethods = emptySet()
                )
            )

            assertThat(videoRepository.findAll(videoIds).map { it.distributionMethods }).isEqualTo(
                listOf(
                    emptySet<DistributionMethodResource>(),
                    emptySet()
                )
            )

            assertThat(fakeEventBus.hasReceivedEventOfType(VideosExclusionFromStreamRequested::class.java)).isTrue()
            assertThat(fakeEventBus.hasReceivedEventOfType(VideosExclusionFromDownloadRequested::class.java)).isTrue()
        }

        @Test
        fun `disabling stream only removes from stream search index and adds to download search index`() {
            val videoIds = listOf(saveVideo(), saveVideo())

            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = videoIds.map { it.value },
                    distributionMethods = setOf(DistributionMethodResource.STREAM)
                )
            )

            assertThat(videoRepository.findAll(videoIds).map { it.distributionMethods })
                .isEqualTo(listOf(setOf(DistributionMethod.STREAM), setOf(DistributionMethod.STREAM)))

            assertThat(fakeEventBus.hasReceivedEventOfType(VideosInclusionInStreamRequested::class.java)).isTrue()
            assertThat(fakeEventBus.hasReceivedEventOfType(VideosExclusionFromDownloadRequested::class.java)).isTrue()
        }

        @Test
        fun `disabling download only removes from download search index and adds to stream search index`() {
            val videoIds = listOf(saveVideo(), saveVideo())

            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = videoIds.map { it.value },
                    distributionMethods = setOf(
                        DistributionMethodResource.DOWNLOAD
                    )
                )
            )

            assertThat(videoRepository.findAll(videoIds).map { it.distributionMethods })
                .isEqualTo(listOf(setOf(DistributionMethod.DOWNLOAD), setOf(DistributionMethod.DOWNLOAD)))

            assertThat(fakeEventBus.hasReceivedEventOfType(VideosInclusionInDownloadRequested::class.java)).isTrue()
            assertThat(fakeEventBus.hasReceivedEventOfType(VideosExclusionFromStreamRequested::class.java)).isTrue()
        }

        @Test
        fun `enabling both adds to both search index`() {
            val videoIds = listOf(saveVideo(), saveVideo())

            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = videoIds.map { it.value },
                    distributionMethods = setOf(DistributionMethodResource.STREAM, DistributionMethodResource.DOWNLOAD)
                )
            )

            assertThat(videoRepository.findAll(videoIds).map { it.distributionMethods }).isEqualTo(
                listOf(
                    DistributionMethod.ALL,
                    DistributionMethod.ALL
                )
            )
            assertThat(fakeEventBus.hasReceivedEventOfType(VideosInclusionInStreamRequested::class.java)).isTrue()
            assertThat(fakeEventBus.hasReceivedEventOfType(VideosInclusionInDownloadRequested::class.java)).isTrue()
        }
    }

    @Test
    fun `null bulkUpdateRequest results in invalid exception thrown`() {
        assertThrows<InvalidBulkUpdateRequestException> { bulkUpdateVideo(null) }
    }
}

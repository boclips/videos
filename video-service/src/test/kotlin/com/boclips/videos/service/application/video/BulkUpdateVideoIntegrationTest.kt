package com.boclips.videos.service.application.video

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
            assertThatChannelHasMessages(topics.videosExclusionFromStreamRequested())
            assertThatChannelHasMessages(topics.videosExclusionFromDownloadRequested())
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
            assertThatChannelHasMessages(topics.videosInclusionInStreamRequested())
            assertThatChannelHasMessages(topics.videosExclusionFromDownloadRequested())
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
            assertThatChannelHasMessages(topics.videosInclusionInDownloadRequested())
            assertThatChannelHasMessages(topics.videosExclusionFromStreamRequested())
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
            assertThatChannelHasMessages(topics.videosInclusionInStreamRequested())
            assertThatChannelHasMessages(topics.videosInclusionInDownloadRequested())
        }
    }

    @Test
    fun `null bulkUpdateRequest results in invalid exception thrown`() {
        assertThrows<InvalidBulkUpdateRequestException> { bulkUpdateVideo(null) }
    }
}

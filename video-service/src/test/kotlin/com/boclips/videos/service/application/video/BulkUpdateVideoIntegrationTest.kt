package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.InvalidBulkUpdateRequestException
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource
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
            val allDeliveryMethods = setOf(
                DeliveryMethodResource.DOWNLOAD,
                DeliveryMethodResource.STREAM
            )
            val videoIds = listOf(saveVideo(), saveVideo())

            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = videoIds.map { it.value },
                    hiddenFromSearchForDeliveryMethods = allDeliveryMethods
                )
            )

            assertThat(videoRepository.findAll(videoIds).map { it.hiddenFromSearchForDistributionMethods })
                .isEqualTo(listOf(DistributionMethod.ALL, DistributionMethod.ALL))
            assertThatChannelHasMessages(topics.videosExclusionFromStreamRequested())
            assertThatChannelHasMessages(topics.videosExclusionFromDownloadRequested())
        }

        @Test
        fun `disabling stream only removes from stream search index and adds to download search index`() {
            val videoIds = listOf(saveVideo(), saveVideo())

            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = videoIds.map { it.value },
                    hiddenFromSearchForDeliveryMethods = setOf(
                        DeliveryMethodResource.STREAM
                    )
                )
            )

            assertThat(videoRepository.findAll(videoIds).map { it.hiddenFromSearchForDistributionMethods })
                .isEqualTo(listOf(setOf(DistributionMethod.STREAM), setOf(DistributionMethod.STREAM)))
            assertThatChannelHasMessages(topics.videosExclusionFromStreamRequested())
            assertThatChannelHasMessages(topics.videosInclusionInDownloadRequested())
        }

        @Test
        fun `disabling download only removes from download search index and adds to stream search index`() {
            val videoIds = listOf(saveVideo(), saveVideo())

            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = videoIds.map { it.value },
                    hiddenFromSearchForDeliveryMethods = setOf(
                        DeliveryMethodResource.DOWNLOAD
                    )
                )
            )

            assertThat(videoRepository.findAll(videoIds).map { it.hiddenFromSearchForDistributionMethods })
                .isEqualTo(listOf(setOf(DistributionMethod.DOWNLOAD), setOf(DistributionMethod.DOWNLOAD)))
            assertThatChannelHasMessages(topics.videosInclusionInStreamRequested())
            assertThatChannelHasMessages(topics.videosExclusionFromDownloadRequested())
        }

        @Test
        fun `enabling both adds to both search index`() {
            val videoIds = listOf(saveVideo(), saveVideo())

            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = videoIds.map { it.value },
                    hiddenFromSearchForDeliveryMethods = emptySet()
                )
            )

            assertThat(videoRepository.findAll(videoIds).map { it.hiddenFromSearchForDistributionMethods })
                .isEqualTo(listOf(emptySet(), emptySet<DistributionMethod>()))
            assertThatChannelHasMessages(topics.videosInclusionInStreamRequested())
            assertThatChannelHasMessages(topics.videosInclusionInDownloadRequested())
        }
    }

    @Test
    fun `null bulkUpdateRequest results in invalid exception thrown`() {
        assertThrows<InvalidBulkUpdateRequestException> { bulkUpdateVideo(null) }
    }
}

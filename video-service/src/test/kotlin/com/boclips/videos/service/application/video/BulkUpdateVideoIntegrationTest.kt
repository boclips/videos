package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.application.video.exceptions.InvalidBulkUpdateRequestException
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.presentation.video.VideoResourceStatus
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class BulkUpdateVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Nested
    inner class UsingDeprecatedStatusField {
        @Test
        fun `disabling sets searchable fields to false, hides from all delivery methods and enqueues removal from search indices`() {
            val videoIds = listOf(saveVideo(searchable = true), saveVideo(searchable = true))

            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = videoIds.map { it.value },
                    status = VideoResourceStatus.SEARCH_DISABLED
                )
            )

            assertThat(videoRepository.findAll(videoIds)).allMatch { !it.searchable }
            assertThat(videoRepository.findAll(videoIds).map { it.hiddenFromSearchForDeliveryMethods })
                .isEqualTo(listOf(DeliveryMethod.ALL, DeliveryMethod.ALL))
            assertThatChannelHasMessages(topics.videosExclusionFromStreamRequested())
            assertThatChannelHasMessages(topics.videosExclusionFromDownloadRequested())
        }

        @Test
        fun `enabling sets searchable fields to true, enables all delivery methods and enqueues addition to search indices`() {
            val videoIds = listOf(saveVideo(searchable = false), saveVideo(searchable = false))
            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = videoIds.map { it.value },
                    status = VideoResourceStatus.SEARCH_DISABLED
                )
            )

            bulkUpdateVideo(BulkUpdateRequest(ids = videoIds.map { it.value }, status = VideoResourceStatus.SEARCHABLE))

            assertThat(videoRepository.findAll(videoIds).flatMap { it.hiddenFromSearchForDeliveryMethods })
                .isEmpty()
            assertThat(videoRepository.findAll(videoIds)).allMatch { it.searchable }
            assertThatChannelHasMessages(topics.videosInclusionInStreamRequested())
            assertThatChannelHasMessages(topics.videosInclusionInDownloadRequested())
        }
    }

    @Nested
    inner class UsingResourceDeliveryMethods {
        @Test
        fun `can disable from all delivery methods`() {
            val allDeliveryMethods = setOf(
                DeliveryMethodResource.DOWNLOAD,
                DeliveryMethodResource.STREAM
            )
            val videoIds = listOf(
                saveVideo(
                    searchable = true,
                    hiddenFromSearchForDeliveryMethods = emptySet()
                ), saveVideo(
                    searchable = true, hiddenFromSearchForDeliveryMethods = emptySet()
                )
            )

            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = videoIds.map { it.value },
                    status = null,
                    hiddenFromSearchForDeliveryMethods = allDeliveryMethods
                )
            )

            assertThat(videoRepository.findAll(videoIds)).allMatch { !it.searchable }
            assertThat(videoRepository.findAll(videoIds).map { it.hiddenFromSearchForDeliveryMethods })
                .isEqualTo(listOf(DeliveryMethod.ALL, DeliveryMethod.ALL))
            assertThatChannelHasMessages(topics.videosExclusionFromStreamRequested())
            assertThatChannelHasMessages(topics.videosExclusionFromDownloadRequested())
        }

        @Test
        fun `disabling stream only removes from stream search index and adds to download search index`() {
            val videoIds = listOf(
                saveVideo(
                    searchable = true,
                    hiddenFromSearchForDeliveryMethods = emptySet()
                ), saveVideo(
                    searchable = true, hiddenFromSearchForDeliveryMethods = emptySet()
                )
            )

            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = videoIds.map { it.value },
                    status = null,
                    hiddenFromSearchForDeliveryMethods = setOf(
                        DeliveryMethodResource.STREAM
                    )
                )
            )

            assertThat(videoRepository.findAll(videoIds)).allMatch { it.searchable }
            assertThat(videoRepository.findAll(videoIds).map { it.hiddenFromSearchForDeliveryMethods })
                .isEqualTo(listOf(setOf(DeliveryMethod.STREAM), setOf(DeliveryMethod.STREAM)))
            assertThatChannelHasMessages(topics.videosExclusionFromStreamRequested())
            assertThatChannelHasMessages(topics.videosInclusionInDownloadRequested())
        }

        @Test
        fun `disabling download only removes from download search index and adds to stream search index`() {
            val videoIds = listOf(
                saveVideo(
                    searchable = false,
                    hiddenFromSearchForDeliveryMethods = emptySet()
                ), saveVideo(
                    searchable = false, hiddenFromSearchForDeliveryMethods = emptySet()
                )
            )

            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = videoIds.map { it.value },
                    status = null,
                    hiddenFromSearchForDeliveryMethods = setOf(
                        DeliveryMethodResource.DOWNLOAD
                    )
                )
            )

            assertThat(videoRepository.findAll(videoIds)).allMatch { it.searchable }
            assertThat(videoRepository.findAll(videoIds).map { it.hiddenFromSearchForDeliveryMethods })
                .isEqualTo(listOf(setOf(DeliveryMethod.DOWNLOAD), setOf(DeliveryMethod.DOWNLOAD)))
            assertThatChannelHasMessages(topics.videosInclusionInStreamRequested())
            assertThatChannelHasMessages(topics.videosExclusionFromDownloadRequested())
        }

        @Test
        fun `enabling both adds to both search index`() {
            val videoIds = listOf(
                saveVideo(
                    searchable = false,
                    hiddenFromSearchForDeliveryMethods = setOf(DeliveryMethodResource.DOWNLOAD)
                ), saveVideo(
                    searchable = false, hiddenFromSearchForDeliveryMethods = setOf(DeliveryMethodResource.STREAM)
                )
            )

            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = videoIds.map { it.value },
                    status = null,
                    hiddenFromSearchForDeliveryMethods = emptySet()
                )
            )

            assertThat(videoRepository.findAll(videoIds)).allMatch { it.searchable }
            assertThat(videoRepository.findAll(videoIds).map { it.hiddenFromSearchForDeliveryMethods })
                .isEqualTo(listOf(emptySet(), emptySet<DeliveryMethod>()))
            assertThatChannelHasMessages(topics.videosInclusionInStreamRequested())
            assertThatChannelHasMessages(topics.videosInclusionInDownloadRequested())
        }
    }

    @Test
    fun `null bulkUpdateRequest results in invalid exception thrown`() {
        assertThrows<InvalidBulkUpdateRequestException> { bulkUpdateVideo(null) }
    }
}

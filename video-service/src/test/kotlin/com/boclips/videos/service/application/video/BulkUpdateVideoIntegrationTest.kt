package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.application.video.exceptions.InvalidBulkUpdateRequestException
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.presentation.video.VideoResourceDeliveryMethod
import com.boclips.videos.service.presentation.video.VideoResourceStatus
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.isNull
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class BulkUpdateVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Nested
    inner class UsingDeprecatedStatusField {
        @Test
        fun `disabling sets searchable field on video to false, hides from all delivery methods and removes from search indices`() {
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

            assertThat(videoSearchService.count(VideoQuery(ids = videoIds.map { it.value }))).isEqualTo(0)
            verify(legacyVideoSearchService).bulkRemoveFromSearch(videoIds.map { it.value })
        }

        @Test
        fun `enabling sets searchable field on video to true, enables all delivery methods and registers in search indices`() {
            val videoIds = listOf(saveVideo(searchable = false), saveVideo(searchable = false))
            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = videoIds.map { it.value },
                    status = VideoResourceStatus.SEARCH_DISABLED
                )
            )

            bulkUpdateVideo(BulkUpdateRequest(ids = videoIds.map { it.value }, status = VideoResourceStatus.SEARCHABLE))

            assertThat(videoRepository.findAll(videoIds)).allMatch { it.searchable }
            assertThat(videoSearchService.count(VideoQuery(ids = videoIds.map { it.value }))).isEqualTo(2)
            verify(legacyVideoSearchService, times(1)).upsert(any(), anyOrNull())
        }

        @Test
        fun `disabling YouTube videos does not register them in the legacy search index`() {
            val videoId = saveVideo(
                searchable = false,
                playbackId = PlaybackId(PlaybackProviderType.YOUTUBE, value = "ref-id-${UUID.randomUUID()}")
            )

            bulkUpdateVideo(
                BulkUpdateRequest(
                    ids = listOf(videoId.value),
                    status = VideoResourceStatus.SEARCH_DISABLED
                )
            )

            bulkUpdateVideo(BulkUpdateRequest(ids = listOf(videoId.value), status = VideoResourceStatus.SEARCHABLE))

            verify(legacyVideoSearchService).upsert(argThat { toList().isEmpty() }, isNull())
        }
    }

    @Nested
    inner class UsingResourceDeliveryMethods {
        @Test
        fun `can disable from all delivery methods`() {
            val allDeliveryMethods = setOf(
                VideoResourceDeliveryMethod.DOWNLOAD,
                VideoResourceDeliveryMethod.STREAM
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

            assertThat(videoSearchService.count(VideoQuery(ids = videoIds.map { it.value }))).isEqualTo(0)
            verify(legacyVideoSearchService).bulkRemoveFromSearch(videoIds.map { it.value })
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
                        VideoResourceDeliveryMethod.STREAM
                    )
                )
            )

            assertThat(videoRepository.findAll(videoIds)).allMatch { it.searchable }
            assertThat(videoRepository.findAll(videoIds).map { it.hiddenFromSearchForDeliveryMethods })
                .isEqualTo(listOf(setOf(DeliveryMethod.STREAM), setOf(DeliveryMethod.STREAM)))

            assertThat(videoSearchService.count(VideoQuery(ids = videoIds.map { it.value }))).isEqualTo(0)

            verify(legacyVideoSearchService, times(3)).upsert(any(), anyOrNull())
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
                        VideoResourceDeliveryMethod.DOWNLOAD
                    )
                )
            )

            assertThat(videoRepository.findAll(videoIds)).allMatch { it.searchable }
            assertThat(videoRepository.findAll(videoIds).map { it.hiddenFromSearchForDeliveryMethods })
                .isEqualTo(listOf(setOf(DeliveryMethod.DOWNLOAD), setOf(DeliveryMethod.DOWNLOAD)))

            assertThat(videoSearchService.count(VideoQuery(ids = videoIds.map { it.value }))).isEqualTo(2)
            verify(legacyVideoSearchService).bulkRemoveFromSearch(videoIds.map { it.value })
        }

        @Test
        fun `enabling both adds to both search index`() {
            val videoIds = listOf(
                saveVideo(
                    searchable = false,
                    hiddenFromSearchForDeliveryMethods = setOf(VideoResourceDeliveryMethod.DOWNLOAD)
                ), saveVideo(
                    searchable = false, hiddenFromSearchForDeliveryMethods = setOf(VideoResourceDeliveryMethod.STREAM)
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

            assertThat(videoSearchService.count(VideoQuery(ids = videoIds.map { it.value }))).isEqualTo(2)
            verify(legacyVideoSearchService, times(1)).upsert(any(), anyOrNull())
        }
    }

    @Test
    fun `null bulkUpdateRequest results in invalid exception thrown`() {
        assertThrows<InvalidBulkUpdateRequestException> { bulkUpdateVideo(null) }
    }
}

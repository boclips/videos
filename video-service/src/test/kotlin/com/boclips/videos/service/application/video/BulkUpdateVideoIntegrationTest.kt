package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideoUpdated
import com.boclips.eventbus.events.video.VideosUpdated
import com.boclips.videos.service.application.video.exceptions.InvalidBulkUpdateRequestException
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class BulkUpdateVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `bulk updates can update distribution methods and publish video updated event`() {
        val videoIds = listOf(saveVideo(), saveVideo())

        bulkUpdateVideo(
            BulkUpdateRequest(
                ids = videoIds.map { it.value },
                distributionMethods = emptySet()
            )
        )

        assertThat(videoRepository.findAll(videoIds).map { it.distributionMethods }).isEqualTo(
            listOf(
                emptySet<DistributionMethod>(),
                emptySet()
            )
        )

        assertThat(fakeEventBus.countEventsOfType(VideosUpdated::class.java)).isEqualTo(1)
        assertThat(fakeEventBus.getEventsOfType(VideosUpdated::class.java).first().videos).hasSize(2)
    }

    @Test
    fun `bulk update throw when request is null`() {
        assertThrows<InvalidBulkUpdateRequestException> { bulkUpdateVideo(null) }
    }
}

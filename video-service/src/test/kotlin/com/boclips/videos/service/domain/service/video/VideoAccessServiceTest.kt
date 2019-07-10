package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoAccessServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoAccessService: VideoAccessService

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `revoking access hides from all delivery methods`() {
        val videoId = saveVideo(hiddenFromSearchForDeliveryMethods = emptySet())

        videoAccessService.revokeAccess(videoIds = listOf(videoId))

        val video = videoRepository.find(videoId = videoId)!!
        assertThat(video.hiddenFromSearchForDeliveryMethods).isEqualTo(
            setOf(
                DeliveryMethod.DOWNLOAD,
                DeliveryMethod.STREAM
            )
        )
    }

    @Test
    fun `granting access enables on all delivery methods`() {
        val videoId = saveVideo(hiddenFromSearchForDeliveryMethods = setOf(DeliveryMethodResource.STREAM))

        videoAccessService.grantAccess(videoIds = listOf(videoId))

        val video = videoRepository.find(videoId = videoId)!!
        assertThat(video.hiddenFromSearchForDeliveryMethods).isEmpty()
    }

    @Test
    fun `blacklists on a subset of delivery methods`() {
        val videoId = saveVideo(hiddenFromSearchForDeliveryMethods = emptySet())

        videoAccessService.setSearchBlacklist(
            videoIds = listOf(videoId),
            deliveryMethods = setOf(DeliveryMethod.STREAM)
        )

        val video = videoRepository.find(videoId = videoId)!!
        assertThat(video.hiddenFromSearchForDeliveryMethods).isEqualTo(
            setOf(
                DeliveryMethod.STREAM
            )
        )
    }
}

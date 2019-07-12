package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.VideoRepository
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
        val videoId = saveVideo()

        videoAccessService.revokeAccess(videoIds = listOf(videoId))

        val video = videoRepository.find(videoId = videoId)!!
        assertThat(video.hiddenFromSearchForDistributionMethods).isEqualTo(
            setOf(
                DistributionMethod.DOWNLOAD,
                DistributionMethod.STREAM
            )
        )
    }

    @Test
    fun `granting access enables on all delivery methods`() {
        val videoId = saveVideo()

        videoAccessService.grantAccess(videoIds = listOf(videoId))

        val video = videoRepository.find(videoId = videoId)!!
        assertThat(video.hiddenFromSearchForDistributionMethods).isEmpty()
    }

    @Test
    fun `blacklists on a subset of delivery methods`() {
        val videoId = saveVideo()

        videoAccessService.setSearchBlacklist(
            videoIds = listOf(videoId),
            distributionMethods = setOf(DistributionMethod.STREAM)
        )

        val video = videoRepository.find(videoId = videoId)!!
        assertThat(video.hiddenFromSearchForDistributionMethods).isEqualTo(
            setOf(
                DistributionMethod.STREAM
            )
        )
    }
}

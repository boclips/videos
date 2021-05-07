package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.UnsupportedVideoUpdateException
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class VideoUpdateServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoUpdateService: VideoUpdateService

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `cannot update a youtube video's title`() {
        val youtubeVideoId = saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "abc"),
            title = "elephant goes to barbados on holibobs"
        )

        val youtubeVideo = videoRepository.find(youtubeVideoId)!!

        assertThrows<UnsupportedVideoUpdateException> {
            videoUpdateService.update(
                youtubeVideo,
                listOf(
                    VideoUpdateCommand.ReplaceTitle(
                        videoId = youtubeVideoId,
                        title = "giraffe goes to vietnam on a business trip"
                    )
                )
            )
        }
    }

    @Test
    fun `cannot update a youtube video's description`() {
        val youtubeVideoId = saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "abc"),
            description = "elephant goes to barbados on holibobs"
        )

        val youtubeVideo = videoRepository.find(youtubeVideoId)!!

        assertThrows<UnsupportedVideoUpdateException> {
            videoUpdateService.update(
                youtubeVideo,
                listOf(
                    VideoUpdateCommand.ReplaceDescription(
                        videoId = youtubeVideoId,
                        description = "giraffe goes to vietnam on a business trip"
                    )
                )
            )
        }
    }

    @Test
    fun `can update a boclips hosted video's title`() {
        val videoId = saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "abc"),
            title = "elephant goes to barbados on holibobs"
        )

        val videoToUpdate = videoRepository.find(videoId)!!
        videoUpdateService.update(
            videoToUpdate,
            listOf(
                VideoUpdateCommand.ReplaceTitle(
                    videoId = videoId,
                    title = "giraffe goes to vietnam on a business trip"
                )
            )
        )

        assertThat(videoRepository.find(videoId)!!.title).isEqualTo("giraffe goes to vietnam on a business trip")
    }
}
package com.boclips.videos.service.infrastructure.event

import com.boclips.eventtypes.VideoToAnalyse
import com.boclips.videos.service.config.VideosToAnalyse
import com.boclips.videos.service.domain.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.test.binder.MessageCollector

class SpringCloudEventServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var eventService: SpringCloudEventService

    @Autowired
    lateinit var messageCollector: MessageCollector

    @Autowired
    lateinit var videosToAnalyse : VideosToAnalyse

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `analyses a video`() {
        val videoId = TestFactories.aValidId()
        val video = TestFactories.createVideo(
            videoAsset = TestFactories.createVideoAsset(videoId = videoId),
            videoPlayback = TestFactories.createKalturaPlayback(downloadUrl = "test_url")
        )
        eventService.analyseVideo(video)

        val message = messageCollector.forChannel(videosToAnalyse.output()).poll()
        assertThat(message.payload).isNotNull

        val messagePayload = objectMapper.readValue(message.payload.toString(), VideoToAnalyse::class.java)

        assertThat(messagePayload.videoId).isEqualTo(videoId)
        assertThat(messagePayload.videoUrl).isEqualTo("test_url")
    }

    @Test
    fun `throws exception for a YouTube video`() {
        val videoId = TestFactories.aValidId()
        val video = TestFactories.createVideo(
            videoAsset = TestFactories.createVideoAsset(videoId = videoId),
            videoPlayback = TestFactories.createYoutubePlayback()
        )

        assertThrows<VideoNotAnalysableException> { eventService.analyseVideo(video) }
    }
}
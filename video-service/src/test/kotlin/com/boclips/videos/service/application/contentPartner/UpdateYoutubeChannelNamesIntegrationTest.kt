package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.infrastructure.contentPartner.MongoContentPartnerRepository
import com.boclips.videos.service.infrastructure.video.mongo.MongoVideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

internal class UpdateYoutubeChannelNamesIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var updateYoutubeChannelNames: UpdateYoutubeChannelNames

    @Autowired
    lateinit var videoRepository: MongoVideoRepository

    @Autowired
    lateinit var contentPartnerRepository: MongoContentPartnerRepository

    @Test
    fun `updates youtube channel name`() {
        val playbackId = TestFactories.createYoutubePlayback().id
        val videoId = saveVideo(
            playbackId = playbackId,
            contentProvider = "Boclips Teacher"
        )

        fakeYoutubePlaybackProvider.clear()
        fakeYoutubePlaybackProvider.addVideo(
            youtubeId = playbackId.value, thumbnailUrl = "123", duration = Duration.ofSeconds(10)
        )
        fakeYoutubePlaybackProvider.addMetadata(
            youtubeId = playbackId.value,
            channelName = "aChannelName",
            channelId = "aChannelId"
        )

        updateYoutubeChannelNames()

        val updatedAsset = videoRepository.find(videoId)!!

        assertThat(
            contentPartnerRepository.findById(
                ContentPartnerId(value = "aChannelId")
            )
        ).isNotNull

        assertThat(updatedAsset.playback).isNotNull
        assertThat(updatedAsset.contentPartner.name).isEqualTo("aChannelName")
        assertThat(updatedAsset.contentPartner.contentPartnerId.value).isEqualTo("aChannelId")
    }
}
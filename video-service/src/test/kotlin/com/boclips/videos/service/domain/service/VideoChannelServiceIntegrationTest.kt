package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoChannelServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoChannelService: VideoChannelService

    @Test
    fun `can find all channels by ids`() {
        val channel1 = saveChannel(name = "1")
        val channel2 = saveChannel(name = "2")
        saveChannel(name = "not me")

        val channels = videoChannelService.findAllByIds(
            listOf(
                ChannelId(value = channel1.id.value),
                ChannelId(value = channel2.id.value),
            )
        )

        assertThat(channels.map { it.channelId.value })
            .containsExactlyInAnyOrder(channel1.id.value, channel2.id.value)
    }
}

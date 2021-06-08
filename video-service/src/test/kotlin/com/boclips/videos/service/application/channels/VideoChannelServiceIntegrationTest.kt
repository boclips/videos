package com.boclips.videos.service.application.channels

import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.CategoryFactory
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

    @Test
    fun `can find channel with its categories`() {
        taxonomyRepository.create(CategoryFactory.sample(code = "A", description = "A description"))
        val channel = saveChannel(name = "1", categories = listOf("A"))
        val retrieved = videoChannelService.findChannelWithCategories(channel.id.value)

        assertThat(retrieved!!.categories).containsExactlyInAnyOrder(
            CategoryWithAncestors(codeValue = CategoryCode("A"), description = "A description", ancestors = emptySet()),

        )
    }
}

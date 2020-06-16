package com.boclips.videos.service.domain.service.video

import com.boclips.videos.api.request.channel.AgeRangeRequest
import com.boclips.videos.service.domain.model.FixedAgeRange
import com.boclips.videos.service.domain.model.UnknownAgeRange
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoCreationServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoCreationService: VideoCreationService

    @Test
    fun `create video with no age range`() {
        createAgeRange(
            AgeRangeRequest(
                id = "early-years",
                min = 3,
                max = 7,
                label = "3-7"
            )
        )
        val contentPartner = saveContentPartner(
            name = "Our content partner",
            ageRanges = listOf("early-years")
        )

        val video = videoCreationService.create(
            TestFactories.createVideo(
                channelName = "Our content partner",
                channelId = ChannelId(
                    value = contentPartner.id.value
                ),
                ageRange = UnknownAgeRange
            )
        )

        assertThat(video.ageRange).isEqualTo(FixedAgeRange(min = 3, max = 7, curatedManually = false))
    }

    @Test
    fun `do not create video when duplicate`() {
        val contentPartner = saveContentPartner(name = "Our content partner")

        videoCreationService.create(
            TestFactories.createVideo(
                channelId = ChannelId(
                    value = contentPartner.id.value
                ),
                videoReference = "video-123"
            )
        )

        org.junit.jupiter.api.assertThrows<VideoNotCreatedException> {
            videoCreationService.create(
                TestFactories.createVideo(
                    channelId = ChannelId(
                        value = contentPartner.id.value
                    ),
                    videoReference = "video-123"
                )
            )
        }
    }

    @Test
    fun `create video with best for tags`() {
        val tagLabel = "explainer"
        val video = videoCreationService.create(
            TestFactories.createVideo(
                tags = listOf(TestFactories.createUserTag(label = tagLabel))
            )
        )

        assertThat(video.tags.first().tag.label).isEqualTo(tagLabel)
    }
}

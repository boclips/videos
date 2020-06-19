package com.boclips.videos.service.application

import com.boclips.eventbus.domain.AgeRange
import com.boclips.eventbus.domain.contentpartner.*
import com.boclips.eventbus.domain.video.VideoType
import com.boclips.eventbus.events.video.VideoUpdated
import com.boclips.eventbus.events.video.VideosUpdated
import com.boclips.videos.service.domain.model.FixedAgeRange
import com.boclips.videos.service.domain.model.UnknownAgeRange
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ChannelUpdatedTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `updates name, legal restrictions, content types, and age ranges`() {
        val channel = TestFactories.createChannel(name = "test-999")
        val video = videoRepository.create(
            TestFactories.createVideo(
                channel = channel,
                legalRestrictions = "some restrictions",
                ageRange = UnknownAgeRange,
                types = listOf(ContentType.STOCK)
            )
        )

        fakeEventBus.publish(
            com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated.builder()
                .contentPartner(
                    Channel.builder()
                        .id(ChannelId(channel.channelId.value))
                        .name("test-888")
                        .legalRestrictions("some better restrictions")
                        .pedagogy(ChannelPedagogyDetails.builder().ageRange(AgeRange.builder().min(10).max(15).build()).build())
                        .ingest(ChannelIngestDetails.builder().type("MANUAL").build())
                        .details(ChannelTopLevelDetails.builder().contentTypes(listOf("NEWS", "INSTRUCTIONAL")).build())
                        .build()
                )
                .build()
        )

        val updatedVideo = videoRepository.find(video.videoId)!!

        assertThat(updatedVideo.channel.name).isEqualTo("test-888")
        assertThat(updatedVideo.legalRestrictions).isEqualTo("some better restrictions")
        assertThat(updatedVideo.ageRange).isEqualTo(FixedAgeRange(10, 15, curatedManually = false))
        assertThat(updatedVideo.types).containsExactly(ContentType.NEWS, ContentType.INSTRUCTIONAL_CLIPS)

        val videoUpdatedEvents = fakeEventBus.getEventsOfType(VideosUpdated::class.java)
        assertThat(videoUpdatedEvents).hasSize(1)
        assertThat(videoUpdatedEvents[0].videos[0].types).containsExactly(VideoType.NEWS, VideoType.INSTRUCTIONAL)
    }

    @Test
    fun `does not update age ranges if set manually`() {
        val channel = TestFactories.createChannel(name = "test-999")
        val video = videoRepository.create(
            TestFactories.createVideo(
                channel = channel,
                legalRestrictions = "some restrictions",
                ageRange = FixedAgeRange(min = 3, max = 5, curatedManually = true)
            )
        )

        fakeEventBus.publish(
            com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated.builder()
                .contentPartner(
                    Channel.builder()
                        .id(ChannelId(channel.channelId.value))
                        .name("test-888")
                        .legalRestrictions("some better restrictions")
                        .pedagogy(ChannelPedagogyDetails.builder().ageRange(AgeRange.builder().min(10).max(15).build()).build())
                        .ingest(ChannelIngestDetails.builder().type("MANUAL").build())
                        .build()
                )
                .build()
        )

        val updatedVideo = videoRepository.find(videoId = video.videoId)!!

        assertThat(updatedVideo.ageRange).isEqualTo(FixedAgeRange(min = 3, max = 5, curatedManually = true))
    }


    @Test
    fun `does not update content types if empty`() {
        val channel = TestFactories.createChannel(name = "test-999")
        val video = videoRepository.create(
            TestFactories.createVideo(
                channel = channel,
                types = listOf(ContentType.STOCK)
            )
        )

        fakeEventBus.publish(
            com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated.builder()
                .contentPartner(
                    Channel.builder()
                        .id(ChannelId(channel.channelId.value))
                        .name("test-888")
                        .details(ChannelTopLevelDetails.builder().contentTypes(emptyList()).build())
                        .build()
                )
                .build()
        )

        val updatedVideo = videoRepository.find(videoId = video.videoId)!!

        assertThat(updatedVideo.channel.name).isEqualTo("test-888")
        assertThat(updatedVideo.types).isEqualTo(listOf(ContentType.STOCK))
    }

    @Test
    fun `updates videos of content partner`() {
        val channel = TestFactories.createChannel(name = "test-999")
        val video1 = videoRepository.create(TestFactories.createVideo(channel = channel))
        val video2 = videoRepository.create(TestFactories.createVideo(channel = channel))
        val video3 =
            videoRepository.create(TestFactories.createVideo(channel = TestFactories.createChannel()))

        fakeEventBus.publish(
            com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated.builder()
                .contentPartner(
                    Channel.builder()
                        .id(ChannelId(channel.channelId.value))
                        .name("test-888")
                        .legalRestrictions("some better restrictions")
                        .ingest(ChannelIngestDetails.builder().type("MANUAL").build())
                        .build()
                )
                .build()
        )

        videoRepository.find(video1.videoId).let {
            assertThat(it!!.channel.name).isEqualTo("test-888")
        }

        videoRepository.find(video2.videoId).let {
            assertThat(it!!.channel.name).isEqualTo("test-888")
        }

        videoRepository.find(video3.videoId).let {
            assertThat(it!!.channel.name).isNotEqualTo("test-888")
        }
    }
}

package com.boclips.videos.service.application

import com.boclips.eventbus.domain.AgeRange
import com.boclips.eventbus.domain.contentpartner.*
import com.boclips.videos.service.domain.model.FixedAgeRange
import com.boclips.videos.service.domain.model.UnknownAgeRange
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.Locale
import com.boclips.eventbus.domain.category.CategoryWithAncestors as EventCategoryWithAncestors

class ChannelUpdatedTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `updates name, legal restrictions, age ranges and language`() {
        val channel = TestFactories.createChannel(name = "test-999")
        val video = videoRepository.create(
            TestFactories.createVideo(
                channel = channel,
                legalRestrictions = "some restrictions",
                ageRange = UnknownAgeRange,
                types = listOf(VideoType.STOCK)
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
                        .details(
                            ChannelTopLevelDetails.builder().contentTypes(listOf("NEWS", "INSTRUCTIONAL")).language(
                                Locale.JAPANESE
                            ).build()
                        )
                        .build()
                )
                .build()
        )

        val updatedVideo = videoRepository.find(video.videoId)!!

        assertThat(updatedVideo.channel.name).isEqualTo("test-888")
        assertThat(updatedVideo.legalRestrictions).isEqualTo("some better restrictions")
        assertThat(updatedVideo.ageRange).isEqualTo(FixedAgeRange(10, 15, curatedManually = false))
        assertThat(updatedVideo.voice.language).isEqualTo(Locale.JAPANESE)
    }

    @Test
    fun `updates channel categories on videos`() {
        val channel = TestFactories.createChannel(name = "test-999")
        val video = videoRepository.create(TestFactories.createVideo(channel = channel))
        val otherVideo = videoRepository.create(TestFactories.createVideo(channel = channel))

        val musicCategory = EventCategoryWithAncestors.builder().code("M").description("Music").build()
        fakeEventBus.publish(
            com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated.builder()
                .contentPartner(
                    Channel.builder()
                        .id(ChannelId(channel.channelId.value))
                        .categories(setOf(musicCategory))
                        .name(channel.name)
                        .build()
                )
                .build()
        )

        val updatedVideo = videoRepository.find(video.videoId)!!
        val otherUpdatedVideo = videoRepository.find(otherVideo.videoId)!!
        val expectedCategory = CategoryWithAncestors(
            codeValue = CategoryCode(musicCategory.code),
            description = musicCategory.description,
            ancestors = emptySet()
        )

        assertThat(updatedVideo.categories!![CategorySource.CHANNEL]).containsOnly(expectedCategory)
        assertThat(otherUpdatedVideo.categories!![CategorySource.CHANNEL]).containsOnly(expectedCategory)
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
    fun `does not cascade down content type changes to videos`() {
        val channel = TestFactories.createChannel(name = "test-999")
        val video = videoRepository.create(
            TestFactories.createVideo(
                channel = channel,
                types = listOf(VideoType.INSTRUCTIONAL_CLIPS)
            )
        )

        fakeEventBus.publish(
            com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated.builder()
                .contentPartner(
                    Channel.builder()
                        .id(ChannelId(channel.channelId.value))
                        .name("test-888")
                        .details(ChannelTopLevelDetails.builder().contentTypes(listOf("NEWS", "INSTRUCTIONAL")).build())
                        .build()
                )
                .build()
        )

        val videoAfterChannelUpdate = videoRepository.find(videoId = video.videoId)!!

        assertThat(videoAfterChannelUpdate.channel.name).isEqualTo("test-888")
        assertThat(videoAfterChannelUpdate.types).isEqualTo(listOf(VideoType.INSTRUCTIONAL_CLIPS))
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

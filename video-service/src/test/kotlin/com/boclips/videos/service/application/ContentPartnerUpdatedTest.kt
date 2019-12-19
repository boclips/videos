package com.boclips.videos.service.application

import com.boclips.eventbus.domain.AgeRange
import com.boclips.eventbus.domain.contentpartner.ContentPartner
import com.boclips.eventbus.domain.contentpartner.ContentPartnerId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ContentPartnerUpdatedTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `update videos of updated content partner`() {
        val contentPartner = TestFactories.createContentPartner(name = "test-999")
        val video = videoRepository.create(
            TestFactories.createVideo(
                contentPartner = contentPartner,
                legalRestrictions = "some restrictions",
                ageRange = com.boclips.videos.service.domain.model.AgeRange.unbounded()
            )
        )

        fakeEventBus.publish(
            com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated.builder()
                .contentPartner(
                    ContentPartner.builder()
                        .id(ContentPartnerId(contentPartner.contentPartnerId.value))
                        .name("test-888")
                        .legalRestrictions("some better restrictions")
                        .ageRange(AgeRange.builder().min(10).max(15).build())
                        .build()
                )
                .build()
        )

        val updatedVideo = videoRepository.find(video.videoId)!!

        assertThat(updatedVideo.contentPartner.name).isEqualTo("test-888")
        assertThat(updatedVideo.legalRestrictions).isEqualTo("some better restrictions")
        assertThat(updatedVideo.ageRange.min()).isEqualTo(10)
        assertThat(updatedVideo.ageRange.max()).isEqualTo(15)
    }

    @Test
    fun `updates multiple videos`() {
        val contentPartner = TestFactories.createContentPartner(name = "test-999")
        val video1 = videoRepository.create(TestFactories.createVideo(contentPartner = contentPartner))
        val video2 = videoRepository.create(TestFactories.createVideo(contentPartner = contentPartner))
        val video3 =
            videoRepository.create(TestFactories.createVideo(contentPartner = TestFactories.createContentPartner()))

        fakeEventBus.publish(
            com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated.builder()
                .contentPartner(
                    ContentPartner.builder()
                        .id(ContentPartnerId(contentPartner.contentPartnerId.value))
                        .name("test-888")
                        .legalRestrictions("some better restrictions")
                        .ageRange(AgeRange.builder().min(10).max(15).build())
                        .build()
                )
                .build()
        )

        videoRepository.find(video1.videoId).let {
            assertThat(it!!.contentPartner.name).isEqualTo("test-888")
        }

        videoRepository.find(video2.videoId).let {
            assertThat(it!!.contentPartner.name).isEqualTo("test-888")
        }

        videoRepository.find(video3.videoId).let {
            assertThat(it!!.contentPartner.name).isNotEqualTo("test-888")
        }
    }
}

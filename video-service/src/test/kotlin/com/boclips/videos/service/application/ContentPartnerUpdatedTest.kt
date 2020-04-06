package com.boclips.videos.service.application

import com.boclips.eventbus.domain.AgeRange
import com.boclips.eventbus.domain.contentpartner.ContentPartner
import com.boclips.eventbus.domain.contentpartner.ContentPartnerId
import com.boclips.eventbus.domain.contentpartner.IngestDetails
import com.boclips.videos.service.domain.model.SpecificAgeRange
import com.boclips.videos.service.domain.model.UnknownAgeRange
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
                ageRange = UnknownAgeRange
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
                        .ingest(IngestDetails.builder().type("MANUAL").build())
                        .build()
                )
                .build()
        )

        val updatedVideo = videoRepository.find(video.videoId)!!

        assertThat(updatedVideo.contentPartner.name).isEqualTo("test-888")
        assertThat(updatedVideo.legalRestrictions).isEqualTo("some better restrictions")
        assertThat(updatedVideo.ageRange).isEqualTo(SpecificAgeRange(10, 15))
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
                        .ingest(IngestDetails.builder().type("MANUAL").build())
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

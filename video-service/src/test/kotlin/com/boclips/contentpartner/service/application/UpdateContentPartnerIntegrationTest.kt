package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.ContentPartnerFilter
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.testsupport.TestFactories
import com.boclips.eventbus.events.video.VideosUpdated
import com.boclips.videos.service.domain.model.common.UnboundedAgeRange
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UpdateContentPartnerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var updateContentPartner: UpdateContentPartner

    @Autowired
    lateinit var contentPartnerRepository: ContentPartnerRepository

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var videoService: VideoService

    @Test
    fun `updating a content partner name`() {
        val originalContentPartner = createContentPartner(
            TestFactories.createContentPartnerRequest(
                name = "My content partner",
                ageRange = AgeRangeRequest(min = 7, max = 11)
            )
        )

        updateContentPartner(
            contentPartnerId = originalContentPartner.contentPartnerId.value,
            request =
            TestFactories.createContentPartnerRequest(
                name = "My better content partner",
                ageRange = AgeRangeRequest(min = 9, max = 14)
            )
        )

        val deletedContentPartner =
            contentPartnerRepository.findAll(listOf(ContentPartnerFilter.NameFilter(name = "My content partner")))

        assertThat(deletedContentPartner).isEmpty()

        val updatedContentPartner = contentPartnerRepository.findAll(
            listOf(
                ContentPartnerFilter.NameFilter(
                    name = "My better content partner"
                )
            )
        ).first()


        assertThat(updatedContentPartner.name).isEqualTo("My better content partner")
    }

    @Test
    fun `updating age ranges of videos`() {
        val originalContentPartner = createContentPartner(
            TestFactories.createContentPartnerRequest(
                name = "My content partner",
                ageRange = AgeRangeRequest(min = 7, max = 11)
            )
        )

        val videoId = saveVideo(
            contentProvider = "My content partner",
            ageRange = UnboundedAgeRange
        )

        updateContentPartner(
            contentPartnerId = originalContentPartner.contentPartnerId.value,
            request = TestFactories.createContentPartnerRequest(
                name = "My better content partner",
                ageRange = AgeRangeRequest(min = 9, max = 14)
            )
        )

        val video = videoService.getPlayableVideo(videoId = videoId)

        assertThat(video.ageRange.min()).isEqualTo(9)
        assertThat(video.ageRange.max()).isEqualTo(14)
    }

    @Test
    fun `disable download and streaming for content partner`() {
        val originalContentPartner = createContentPartner(
            TestFactories.createContentPartnerRequest(
                distributionMethods = emptySet()
            )
        )

        val updatedContentPartner = updateContentPartner(
            contentPartnerId = originalContentPartner.contentPartnerId.value,
            request = TestFactories.createContentPartnerRequest(
                distributionMethods = setOf(
                    DistributionMethodResource.STREAM,
                    DistributionMethodResource.DOWNLOAD
                )
            )
        )

        assertThat(contentPartnerRepository.findById(updatedContentPartner.contentPartnerId)!!.distributionMethods)
            .isEqualTo(DistributionMethod.ALL)
    }

    @Test
    fun `enable downloading and streaming for content partner`() {
        val originalContentPartner = createContentPartner(
            TestFactories.createContentPartnerRequest(
                distributionMethods = setOf(DistributionMethodResource.DOWNLOAD)
            )
        )

        val updatedContentPartner = updateContentPartner(
            contentPartnerId = originalContentPartner.contentPartnerId.value,
            request = TestFactories.createContentPartnerRequest(
                distributionMethods = emptySet()
            )
        )

        assertThat(contentPartnerRepository.findById(updatedContentPartner.contentPartnerId)!!.distributionMethods)
            .isEmpty()
    }

    @Nested
    inner class ContentPartnerVideoRepercussions {
        @Test
        fun `changing distribution methods of content partner will change their videos too`() {
            val originalContentPartner = createContentPartner(
                TestFactories.createContentPartnerRequest(
                    distributionMethods = setOf(DistributionMethodResource.DOWNLOAD)
                )
            )

            val id = saveVideo(contentProviderId = originalContentPartner.contentPartnerId.value)

            updateContentPartner(
                contentPartnerId = originalContentPartner.contentPartnerId.value,
                request = TestFactories.createContentPartnerRequest(
                    distributionMethods = setOf(
                        DistributionMethodResource.STREAM,
                        DistributionMethodResource.DOWNLOAD
                    )
                )
            )

            assertThat(videoRepository.find(id)!!.distributionMethods).isEqualTo(DistributionMethod.ALL)
            assertThat(fakeEventBus.countEventsOfType(VideosUpdated::class.java)).isEqualTo(1)
            assertThat(fakeEventBus.getEventsOfType(VideosUpdated::class.java).first().videos).hasSize(1)
        }
    }
}
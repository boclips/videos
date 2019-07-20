package com.boclips.videos.service.application.contentPartner

import com.boclips.eventbus.events.video.VideosInclusionInStreamRequested
import com.boclips.videos.service.domain.model.common.UnboundedAgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
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

        val deletedContentPartner = contentPartnerRepository.findByName(contentPartnerName = "My content partner")
        assertThat(deletedContentPartner).isNull()

        val updatedContentPartner = contentPartnerRepository.findByName(
            contentPartnerName = "My better content partner"
        )
        assertThat(updatedContentPartner!!.name).isEqualTo("My better content partner")
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
    fun `changing distribution method will enqueue a change for later`() {
        val originalContentPartner = createContentPartner(
            TestFactories.createContentPartnerRequest(
                distributionMethods = emptySet()
            )
        )

        saveVideo(contentProviderId = originalContentPartner.contentPartnerId.value)

        updateContentPartner(
            contentPartnerId = originalContentPartner.contentPartnerId.value,
            request = TestFactories.createContentPartnerRequest(
                distributionMethods = setOf(
                    DistributionMethodResource.STREAM
                )
            )
        )

        assertThat(fakeEventBus.hasReceivedEventOfType(VideosInclusionInStreamRequested::class.java)).isTrue()
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

            val id = saveVideo(
                contentProviderId = originalContentPartner.contentPartnerId.value
            )

            updateContentPartner(
                contentPartnerId = originalContentPartner.contentPartnerId.value,
                request = TestFactories.createContentPartnerRequest(
                    distributionMethods = setOf(
                        DistributionMethodResource.STREAM,
                        DistributionMethodResource.DOWNLOAD
                    )
                )
            )

            assertThat(videoRepository.find(id)!!.distributionMethods).isEqualTo(
                DistributionMethod.ALL
            )
        }
    }
}

package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.common.UnboundedAgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource
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

    @Nested
    inner class UsingDeprecatedSearchableField {

        @Test
        fun `excluding from search enqueues a change for later`() {
            val originalContentPartner = createContentPartner(
                TestFactories.createContentPartnerRequest(searchable = true)
            )

            saveVideo(contentProviderId = originalContentPartner.contentPartnerId.value)

            updateContentPartner(
                contentPartnerId = originalContentPartner.contentPartnerId.value,
                request = TestFactories.createContentPartnerRequest(searchable = false)
            )

            val message = messageCollector.forChannel(topics.videosExclusionFromSearchRequested()).poll()
            assertThat(message).isNotNull
        }

        @Test
        fun `excluding from search sets deprecated state and sets hidden state for all delivery methods`() {
            val originalContentPartner = createContentPartner(
                TestFactories.createContentPartnerRequest(searchable = true)
            )

            val updatedContentPartner = updateContentPartner(
                contentPartnerId = originalContentPartner.contentPartnerId.value,
                request = TestFactories.createContentPartnerRequest(searchable = false)
            )

            assertThat(contentPartnerRepository.findById(updatedContentPartner.contentPartnerId)!!.searchable).isFalse()
            assertThat(contentPartnerRepository.findById(updatedContentPartner.contentPartnerId)!!.hiddenFromSearchForDeliveryMethods).isEqualTo(
                DeliveryMethod.ALL
            )
        }

        @Test
        fun `including from search sets deprecated state and sets hidden state for no delivery methods`() {
            val originalContentPartner = createContentPartner(
                TestFactories.createContentPartnerRequest(searchable = false)
            )

            val updatedContentPartner = updateContentPartner(
                contentPartnerId = originalContentPartner.contentPartnerId.value,
                request = TestFactories.createContentPartnerRequest(searchable = true)
            )

            assertThat(contentPartnerRepository.findById(updatedContentPartner.contentPartnerId)!!.searchable).isTrue()
            assertThat(contentPartnerRepository.findById(updatedContentPartner.contentPartnerId)!!.hiddenFromSearchForDeliveryMethods).isEmpty()
        }

        @Test
        fun `excluding a content partner from search also excludes their videos`() {
            val originalContentPartner = createContentPartner(
                TestFactories.createContentPartnerRequest(searchable = true)
            )

            val id = saveVideo(contentProviderId = originalContentPartner.contentPartnerId.value, searchable = true)

            updateContentPartner(
                contentPartnerId = originalContentPartner.contentPartnerId.value,
                request = TestFactories.createContentPartnerRequest(searchable = false)
            )

            assertThat(videoRepository.find(id)!!.searchable).isFalse()
            assertThat(videoRepository.find(id)!!.hiddenFromSearchForDeliveryMethods).isEqualTo(DeliveryMethod.ALL)
        }

        @Test
        fun `including a content partner from search also includes their videos`() {
            val originalContentPartner = createContentPartner(
                TestFactories.createContentPartnerRequest(searchable = false)
            )

            val id = saveVideo(contentProviderId = originalContentPartner.contentPartnerId.value, searchable = false)

            updateContentPartner(
                contentPartnerId = originalContentPartner.contentPartnerId.value,
                request = TestFactories.createContentPartnerRequest(searchable = true)
            )

            assertThat(videoRepository.find(id)!!.searchable).isTrue()
            assertThat(videoRepository.find(id)!!.hiddenFromSearchForDeliveryMethods).isEmpty()
        }
    }

    @Nested
    inner class UsingResourceDeliveryMethods {
        @Test
        fun `excluding from all delivery methods sets deprecated state and sets hidden state for all delivery methods`() {
            val originalContentPartner = createContentPartner(
                TestFactories.createContentPartnerRequest(
                    searchable = false,
                    hiddenFromSearchForDeliveryMethods = emptySet()
                )
            )

            val updatedContentPartner = updateContentPartner(
                contentPartnerId = originalContentPartner.contentPartnerId.value,
                request = TestFactories.createContentPartnerRequest(
                    searchable = null,
                    hiddenFromSearchForDeliveryMethods = setOf(
                        DeliveryMethodResource.STREAM,
                        DeliveryMethodResource.DOWNLOAD
                    )
                )
            )

            assertThat(contentPartnerRepository.findById(updatedContentPartner.contentPartnerId)!!.searchable).isFalse()
            assertThat(contentPartnerRepository.findById(updatedContentPartner.contentPartnerId)!!.hiddenFromSearchForDeliveryMethods).isEqualTo(
                DeliveryMethod.ALL
            )
        }

        @Test
        fun `including in all delivery methods sets deprecated state and sets hidden state for no delivery methods`() {
            val originalContentPartner = createContentPartner(
                TestFactories.createContentPartnerRequest(
                    searchable = false,
                    hiddenFromSearchForDeliveryMethods = setOf(DeliveryMethodResource.DOWNLOAD)
                )
            )

            val updatedContentPartner = updateContentPartner(
                contentPartnerId = originalContentPartner.contentPartnerId.value,
                request = TestFactories.createContentPartnerRequest(
                    searchable = null,
                    hiddenFromSearchForDeliveryMethods = emptySet()
                )
            )

            assertThat(contentPartnerRepository.findById(updatedContentPartner.contentPartnerId)!!.searchable).isTrue()
            assertThat(contentPartnerRepository.findById(updatedContentPartner.contentPartnerId)!!.hiddenFromSearchForDeliveryMethods).isEmpty()
        }

        @Test
        fun `excluding from some delivery methods sets deprecated state and sets hidden state for given delivery methods`() {
            val originalContentPartner = createContentPartner(
                TestFactories.createContentPartnerRequest(
                    searchable = false,
                    hiddenFromSearchForDeliveryMethods = setOf(DeliveryMethodResource.DOWNLOAD)
                )
            )

            val updatedContentPartner = updateContentPartner(
                contentPartnerId = originalContentPartner.contentPartnerId.value,
                request = TestFactories.createContentPartnerRequest(
                    searchable = null,
                    hiddenFromSearchForDeliveryMethods = setOf(DeliveryMethodResource.STREAM)
                )
            )

            assertThat(contentPartnerRepository.findById(updatedContentPartner.contentPartnerId)!!.searchable).isTrue()
            assertThat(contentPartnerRepository.findById(updatedContentPartner.contentPartnerId)!!.hiddenFromSearchForDeliveryMethods).isEqualTo(
                setOf(DeliveryMethod.STREAM)
            )
        }

        @Test
        fun `excluding a content partner for all delivery methods also excludes their videos`() {
            val originalContentPartner = createContentPartner(
                TestFactories.createContentPartnerRequest(
                    searchable = false,
                    hiddenFromSearchForDeliveryMethods = setOf(DeliveryMethodResource.DOWNLOAD)
                )
            )

            val id = saveVideo(contentProviderId = originalContentPartner.contentPartnerId.value, searchable = false)

            updateContentPartner(
                contentPartnerId = originalContentPartner.contentPartnerId.value,
                request = TestFactories.createContentPartnerRequest(
                    searchable = null,
                    hiddenFromSearchForDeliveryMethods = setOf(
                        DeliveryMethodResource.STREAM,
                        DeliveryMethodResource.DOWNLOAD
                    )
                )
            )

            assertThat(videoRepository.find(id)!!.searchable).isFalse()
            assertThat(videoRepository.find(id)!!.hiddenFromSearchForDeliveryMethods).isEqualTo(DeliveryMethod.ALL)
        }

        @Test
        fun `excluding a content partner for some delivery methods also excludes their videos`() {
            val originalContentPartner = createContentPartner(
                TestFactories.createContentPartnerRequest(
                    searchable = false,
                    hiddenFromSearchForDeliveryMethods = setOf(DeliveryMethodResource.DOWNLOAD)
                )
            )

            val id = saveVideo(contentProviderId = originalContentPartner.contentPartnerId.value, searchable = false)

            updateContentPartner(
                contentPartnerId = originalContentPartner.contentPartnerId.value,
                request = TestFactories.createContentPartnerRequest(
                    searchable = null,
                    hiddenFromSearchForDeliveryMethods = setOf(DeliveryMethodResource.STREAM)
                )
            )

            assertThat(videoRepository.find(id)!!.searchable).isTrue()
            assertThat(videoRepository.find(id)!!.hiddenFromSearchForDeliveryMethods).isEqualTo(setOf(DeliveryMethod.STREAM))
        }

        @Test
        fun `including a content partner for all delivery methods also includes their videos`() {
            val originalContentPartner = createContentPartner(
                TestFactories.createContentPartnerRequest(
                    searchable = false,
                    hiddenFromSearchForDeliveryMethods = setOf(DeliveryMethodResource.DOWNLOAD)
                )
            )

            val id = saveVideo(contentProviderId = originalContentPartner.contentPartnerId.value, searchable = false)

            updateContentPartner(
                contentPartnerId = originalContentPartner.contentPartnerId.value,
                request = TestFactories.createContentPartnerRequest(
                    searchable = null,
                    hiddenFromSearchForDeliveryMethods = emptySet()
                )
            )

            assertThat(videoRepository.find(id)!!.searchable).isTrue()
            assertThat(videoRepository.find(id)!!.hiddenFromSearchForDeliveryMethods).isEmpty()
        }
    }
}

package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.ageRange.UnboundedAgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
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
    fun `excluding a content partner from search`() {
        val originalContentPartner = createContentPartner(
            TestFactories.createContentPartnerRequest(searchable = true)
        )

        val updatedContentPartner = updateContentPartner(
            contentPartnerId = originalContentPartner.contentPartnerId.value,
            request = TestFactories.createContentPartnerRequest(searchable = false)
        )

        assertThat(contentPartnerRepository.findById(updatedContentPartner.contentPartnerId)!!.searchable).isFalse()
    }

    @Test
    fun `including a content partner from search`() {
        val originalContentPartner = createContentPartner(
            TestFactories.createContentPartnerRequest(searchable = false)
        )

        val updatedContentPartner = updateContentPartner(
            contentPartnerId = originalContentPartner.contentPartnerId.value,
            request = TestFactories.createContentPartnerRequest(searchable = true)
        )

        assertThat(contentPartnerRepository.findById(updatedContentPartner.contentPartnerId)!!.searchable).isTrue()
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
    }
}
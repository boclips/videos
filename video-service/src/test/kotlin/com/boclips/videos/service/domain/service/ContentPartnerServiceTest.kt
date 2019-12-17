package com.boclips.videos.service.domain.service

import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.testsupport.TestFactories
import com.boclips.videos.service.domain.model.video.Availability
import com.boclips.videos.service.domain.model.video.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContentPartnerServiceTest {

    @Test
    fun `find content partner availability for all`() {
        val contentPartnerRepository = mockRepository(setOf(DistributionMethod.DOWNLOAD, DistributionMethod.STREAM))

        val contentPartnerService = ContentPartnerService(contentPartnerRepository = contentPartnerRepository)

        val availability =
            contentPartnerService.findAvailabilityFor(contentPartnerId = ContentPartnerId(value = "test"))

        assertThat(availability).isEqualTo(Availability.ALL)
    }

    @Test
    fun `find content partner availability for streaming`() {
        val contentPartnerRepository = mockRepository(setOf(DistributionMethod.STREAM))

        val contentPartnerService = ContentPartnerService(contentPartnerRepository = contentPartnerRepository)

        val availability =
            contentPartnerService.findAvailabilityFor(contentPartnerId = ContentPartnerId(value = "test"))

        assertThat(availability).isEqualTo(Availability.STREAMING)
    }

    @Test
    fun `find content partner availability for download`() {
        val contentPartnerRepository = mockRepository(setOf(DistributionMethod.DOWNLOAD))

        val contentPartnerService = ContentPartnerService(contentPartnerRepository = contentPartnerRepository)

        val availability =
            contentPartnerService.findAvailabilityFor(contentPartnerId = ContentPartnerId(value = "test"))

        assertThat(availability).isEqualTo(Availability.DOWNLOAD)
    }

    @Test
    fun `find content partner availability for empty distribution methods`() {
        val contentPartnerRepository = mockRepository(emptySet())

        val contentPartnerService = ContentPartnerService(contentPartnerRepository = contentPartnerRepository)

        val availability =
            contentPartnerService.findAvailabilityFor(contentPartnerId = ContentPartnerId(value = "test"))

        assertThat(availability).isEqualTo(Availability.NONE)
    }

    private fun mockRepository(distributionMethods: Set<DistributionMethod>): ContentPartnerRepository {
        return mock() {
            on { findById(any()) } doReturn
                TestFactories.createContentPartner(
                    distributionMethods = distributionMethods
                )
        }
    }
}

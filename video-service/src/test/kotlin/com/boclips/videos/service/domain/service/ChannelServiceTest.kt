package com.boclips.videos.service.domain.service

import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import com.boclips.videos.service.domain.model.video.contentpartner.Availability
import com.boclips.videos.service.domain.model.video.contentpartner.ContentPartnerId
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ChannelServiceTest {

    @Test
    fun `find content partner availability for all`() {
        val contentPartnerRepository = mockRepository(setOf(DistributionMethod.DOWNLOAD, DistributionMethod.STREAM))

        val contentPartnerService = ContentPartnerService(channelRepository = contentPartnerRepository)

        val availability =
            contentPartnerService.findAvailabilityFor(contentPartnerId = ContentPartnerId(value = "test"))

        assertThat(availability).isEqualTo(Availability.ALL)
    }

    @Test
    fun `find content partner availability for streaming`() {
        val contentPartnerRepository = mockRepository(setOf(DistributionMethod.STREAM))

        val contentPartnerService = ContentPartnerService(channelRepository = contentPartnerRepository)

        val availability =
            contentPartnerService.findAvailabilityFor(
                contentPartnerId = ContentPartnerId(value = "test")
            )

        assertThat(availability).isEqualTo(Availability.STREAMING)
    }

    @Test
    fun `find content partner availability for download`() {
        val contentPartnerRepository = mockRepository(setOf(DistributionMethod.DOWNLOAD))

        val contentPartnerService = ContentPartnerService(channelRepository = contentPartnerRepository)

        val availability =
            contentPartnerService.findAvailabilityFor(
                contentPartnerId = ContentPartnerId(
                    value = "test"
                )
            )

        assertThat(availability).isEqualTo(Availability.DOWNLOAD)
    }

    @Test
    fun `find content partner availability for empty distribution methods`() {
        val contentPartnerRepository = mockRepository(emptySet())

        val contentPartnerService = ContentPartnerService(channelRepository = contentPartnerRepository)

        val availability =
            contentPartnerService.findAvailabilityFor(contentPartnerId = ContentPartnerId(value = "test"))

        assertThat(availability).isEqualTo(Availability.NONE)
    }

    @Test
    fun `memoises content partner look up`() {
        val contentPartnerRepository = mockRepository(emptySet())

        val contentPartnerService = ContentPartnerService(channelRepository = contentPartnerRepository)

        contentPartnerService.findAvailabilityFor(contentPartnerId = ContentPartnerId(value = "test"))
        contentPartnerService.findAvailabilityFor(contentPartnerId = ContentPartnerId(value = "test"))
        contentPartnerService.findAvailabilityFor(contentPartnerId = ContentPartnerId(value = "test"))
        contentPartnerService.findAvailabilityFor(contentPartnerId = ContentPartnerId(value = "new content partner"))

        verify(contentPartnerRepository, times(2)).findById(any())
    }

    private fun mockRepository(distributionMethods: Set<DistributionMethod>): ChannelRepository {
        return mock() {
            on { findById(any()) } doReturn
                ChannelFactory.createChannel(
                    distributionMethods = distributionMethods
                )
        }
    }
}

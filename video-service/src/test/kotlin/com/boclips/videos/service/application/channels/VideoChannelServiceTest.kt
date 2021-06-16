package com.boclips.videos.service.application.channels

import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.domain.model.video.channel.ContentPartnerAvailability
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoChannelServiceTest {

    @Test
    fun `find channel availability for all`() {
        val contentPartnerRepository = mockRepository(setOf(DistributionMethod.DOWNLOAD, DistributionMethod.STREAM))

        val channelService = VideoChannelService(channelRepository = contentPartnerRepository)

        val availability =
            channelService.findAvailabilityFor(
                channelId = ChannelId(
                    value = "test"
                )
            )

        assertThat(availability).isEqualTo(ContentPartnerAvailability.ALL)
    }

    @Test
    fun `find channel availability for streaming`() {
        val contentPartnerRepository = mockRepository(setOf(DistributionMethod.STREAM))

        val channelService = VideoChannelService(channelRepository = contentPartnerRepository)

        val availability =
            channelService.findAvailabilityFor(
                channelId = ChannelId(
                    value = "test"
                )
            )

        assertThat(availability).isEqualTo(ContentPartnerAvailability.STREAMING)
    }

    @Test
    fun `find channel availability for download`() {
        val contentPartnerRepository = mockRepository(setOf(DistributionMethod.DOWNLOAD))

        val channelService = VideoChannelService(channelRepository = contentPartnerRepository)

        val availability =
            channelService.findAvailabilityFor(
                channelId = ChannelId(
                    value = "test"
                )
            )

        assertThat(availability).isEqualTo(ContentPartnerAvailability.DOWNLOAD)
    }

    @Test
    fun `find channel availability for empty distribution methods`() {
        val contentPartnerRepository = mockRepository(emptySet())

        val channelService = VideoChannelService(channelRepository = contentPartnerRepository)

        val availability =
            channelService.findAvailabilityFor(
                channelId = ChannelId(
                    value = "test"
                )
            )

        assertThat(availability).isEqualTo(ContentPartnerAvailability.NONE)
    }

    @Test
    fun `memoises channel look up`() {
        val contentPartnerRepository = mockRepository(emptySet())

        val channelService = VideoChannelService(channelRepository = contentPartnerRepository)

        channelService.findAvailabilityFor(
            channelId = ChannelId(
                value = "test"
            )
        )
        channelService.findAvailabilityFor(
            channelId = ChannelId(
                value = "test"
            )
        )
        channelService.findAvailabilityFor(
            channelId = ChannelId(
                value = "test"
            )
        )
        channelService.findAvailabilityFor(
            channelId = ChannelId(
                value = "new channel"
            )
        )

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

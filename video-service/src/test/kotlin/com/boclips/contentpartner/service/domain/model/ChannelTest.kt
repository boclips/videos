package com.boclips.contentpartner.service.domain.model

import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import com.boclips.videos.service.domain.model.video.channel.ContentPartnerAvailability
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

internal class ChannelTest {

    @Test
    fun `is streamable if not hidden from stream`() {
        val contentPartner =
            ChannelFactory.createChannel(distributionMethods = setOf(DistributionMethod.STREAM))

        assertThat(contentPartner.isStreamable()).isTrue()
    }

    @ParameterizedTest
    @ArgumentsSource(AvailabilityProvider::class)
    fun `availability is DOWNLOAD when distribution methods is downloadable if not hidden for download`(
        distributionMethods: Set<DistributionMethod>,
        expectedAvailability: ContentPartnerAvailability
    ) {
        val contentPartner =
            ChannelFactory.createChannel(distributionMethods = distributionMethods)

        assertThat(contentPartner.availability()).isEqualTo(expectedAvailability)
    }

    @Test
    fun `is not streamable`() {
        val contentPartner =
            ChannelFactory.createChannel(distributionMethods = emptySet())

        assertThat(contentPartner.isStreamable()).isFalse()
    }

    class AvailabilityProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(setOf(DistributionMethod.STREAM, DistributionMethod.DOWNLOAD), ContentPartnerAvailability.ALL),
                Arguments.of(setOf(DistributionMethod.DOWNLOAD), ContentPartnerAvailability.DOWNLOAD),
                Arguments.of(setOf(DistributionMethod.STREAM), ContentPartnerAvailability.STREAMING),
                Arguments.of(setOf<DistributionMethod>(), ContentPartnerAvailability.NONE),
            )
        }
    }
}

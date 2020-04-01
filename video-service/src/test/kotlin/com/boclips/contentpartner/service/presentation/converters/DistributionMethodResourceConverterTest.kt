package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.contentpartner.DistributionMethod
import com.boclips.contentpartner.service.presentation.converters.DistributionMethodResourceConverter
import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DistributionMethodResourceConverterTest {
    @Test
    fun `converts set of distribution methods from resources`() {
        val deliveryMethodResources = setOf(DistributionMethodResource.DOWNLOAD)

        val convertedDistributionMethod =
            DistributionMethodResourceConverter.toDistributionMethods(deliveryMethodResources)

        assertThat(convertedDistributionMethod).containsExactly(DistributionMethod.DOWNLOAD)
    }

    @Test
    fun `converts set of delivery methods`() {
        val distributionMethods = setOf(DistributionMethod.DOWNLOAD)

        val convertedDeliveryMethods =
            DistributionMethodResourceConverter.toDeliveryMethodResources(distributionMethods)

        assertThat(convertedDeliveryMethods).containsExactly(DistributionMethodResource.DOWNLOAD)
    }
}

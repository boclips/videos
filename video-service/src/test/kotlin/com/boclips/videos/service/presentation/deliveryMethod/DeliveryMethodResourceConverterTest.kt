package com.boclips.videos.service.presentation.deliveryMethod

import com.boclips.videos.service.domain.model.video.DistributionMethod
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DeliveryMethodResourceConverterTest {
    @Test
    fun `converts set of distribution methods from resources`() {
        val deliveryMethodResources = setOf(DeliveryMethodResource.DOWNLOAD)

        val convertedDistributionMethod = DeliveryMethodResourceConverter.toEnabledDistributionMethods(deliveryMethodResources)

        assertThat(convertedDistributionMethod).containsExactly(DistributionMethod.STREAM)
    }

    @Test
    fun `converts set of delivery methods`() {
        val distributionMethods = setOf(DistributionMethod.DOWNLOAD)

        val convertedDeliveryMethods = DeliveryMethodResourceConverter.toDisabledDeliveryMethodResources(distributionMethods)

        assertThat(convertedDeliveryMethods).containsExactly(DeliveryMethodResource.STREAM)
    }
}
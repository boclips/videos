package com.boclips.videos.service.presentation.deliveryMethod

import com.boclips.videos.service.domain.model.video.DistributionMethod

object DeliveryMethodResourceConverter {
    fun toEnabledDistributionMethods(deliveryMethodResources: Set<DeliveryMethodResource>): Set<DistributionMethod> {
        return DistributionMethod.ALL - deliveryMethodResources.map { fromResource(it) }.toSet()
    }

    fun toDisabledDeliveryMethodResources(distributionMethods: Set<DistributionMethod>): Set<DeliveryMethodResource> {
        return (DistributionMethod.ALL - distributionMethods).map { toResource(it) }.toSet()
    }

    //TODO: should be private
    fun toResource(distributionMethod: DistributionMethod) =
        when (distributionMethod) {
            DistributionMethod.DOWNLOAD -> DeliveryMethodResource.DOWNLOAD
            DistributionMethod.STREAM -> DeliveryMethodResource.STREAM
        }
    
    //TODO: should be private
    fun fromResource(videoResourceDeliveryMethod: DeliveryMethodResource) = when (videoResourceDeliveryMethod) {
        DeliveryMethodResource.DOWNLOAD -> DistributionMethod.DOWNLOAD
        DeliveryMethodResource.STREAM -> DistributionMethod.STREAM
    }
}
package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.videos.api.response.contentpartner.DistributionMethodResource

object DistributionMethodResourceConverter {
    fun toDistributionMethods(distributionMethodResources: Set<DistributionMethodResource>): Set<DistributionMethod> {
        return distributionMethodResources.map {
            fromResource(
                it
            )
        }.toSet()
    }

    fun toDeliveryMethodResources(distributionMethods: Set<DistributionMethod>): Set<DistributionMethodResource> {
        return distributionMethods.map {
            toModel(
                it
            )
        }.toSet()
    }

    //TODO: should be private
    fun toModel(distributionMethod: DistributionMethod) =
        when (distributionMethod) {
            DistributionMethod.DOWNLOAD -> DistributionMethodResource.DOWNLOAD
            DistributionMethod.STREAM -> DistributionMethodResource.STREAM
        }

    //TODO: should be private
    fun fromResource(videoResourceDistributionMethod: DistributionMethodResource) =
        when (videoResourceDistributionMethod) {
            DistributionMethodResource.DOWNLOAD -> DistributionMethod.DOWNLOAD
            DistributionMethodResource.STREAM -> DistributionMethod.STREAM
        }
}

package com.boclips.contentpartner.service.infrastructure.contentpartner.converters

import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.infrastructure.UnknownDeliveryMethodException
import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument

object DistributionMethodDocumentConverter {
    fun toDocument(distributionMethod: DistributionMethod): DistributionMethodDocument =
        when (distributionMethod) {
            DistributionMethod.DOWNLOAD -> DistributionMethodDocument(
                DistributionMethodDocument.DELIVERY_METHOD_DOWNLOAD
            )
            DistributionMethod.STREAM -> DistributionMethodDocument(DistributionMethodDocument.DELIVERY_METHOD_STREAM)
        }

    fun fromDocument(document: DistributionMethodDocument): DistributionMethod =
        when (document.deliveryMethod) {
            DistributionMethodDocument.DELIVERY_METHOD_DOWNLOAD -> DistributionMethod.DOWNLOAD
            DistributionMethodDocument.DELIVERY_METHOD_STREAM -> DistributionMethod.STREAM
            else -> throw UnknownDeliveryMethodException()
        }
}

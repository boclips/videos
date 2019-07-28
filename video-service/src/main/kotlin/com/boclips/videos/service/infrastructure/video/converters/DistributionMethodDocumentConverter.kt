package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.video.DistributionMethod
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
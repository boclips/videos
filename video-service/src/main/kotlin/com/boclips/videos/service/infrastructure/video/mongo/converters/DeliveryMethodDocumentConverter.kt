package com.boclips.videos.service.infrastructure.video.mongo.converters

import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.infrastructure.video.mongo.DeliveryMethodDocument

object DeliveryMethodDocumentConverter {
    fun toDocument(deliveryMethod: DeliveryMethod): DeliveryMethodDocument =
        when (deliveryMethod) {
            DeliveryMethod.DOWNLOAD -> DeliveryMethodDocument(DeliveryMethodDocument.DELIVERY_METHOD_DOWNLOAD)
            DeliveryMethod.STREAM -> DeliveryMethodDocument(DeliveryMethodDocument.DELIVERY_METHOD_STREAM)
        }

    fun fromDocument(document: DeliveryMethodDocument): DeliveryMethod =
        when (document.deliveryMethod) {
            DeliveryMethodDocument.DELIVERY_METHOD_DOWNLOAD -> DeliveryMethod.DOWNLOAD
            DeliveryMethodDocument.DELIVERY_METHOD_STREAM -> DeliveryMethod.STREAM
            else -> throw UnknownDeliveryMethod()
        }
}
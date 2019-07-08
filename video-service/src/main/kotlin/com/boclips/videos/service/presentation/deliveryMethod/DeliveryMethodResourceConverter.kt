package com.boclips.videos.service.presentation.deliveryMethod

import com.boclips.videos.service.domain.model.video.DeliveryMethod

object DeliveryMethodResourceConverter {
    fun toResource(deliveryMethod: DeliveryMethod) =
        when (deliveryMethod) {
            DeliveryMethod.DOWNLOAD -> DeliveryMethodResource.DOWNLOAD
            DeliveryMethod.STREAM -> DeliveryMethodResource.STREAM
        }

    fun fromResource(videoResourceDeliveryMethod: DeliveryMethodResource) = when (videoResourceDeliveryMethod) {
        DeliveryMethodResource.DOWNLOAD -> DeliveryMethod.DOWNLOAD
        DeliveryMethodResource.STREAM -> DeliveryMethod.STREAM
    }
}
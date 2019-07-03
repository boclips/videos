package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.video.DeliveryMethod

object VideoResourceDeliveryMethodConverter {
    fun toResource(deliveryMethod: DeliveryMethod) =
        when (deliveryMethod) {
            DeliveryMethod.DOWNLOAD -> VideoResourceDeliveryMethod.DOWNLOAD
            DeliveryMethod.STREAM -> VideoResourceDeliveryMethod.STREAM
        }

    fun fromResource(videoResourceDeliveryMethod: VideoResourceDeliveryMethod) = when (videoResourceDeliveryMethod) {
        VideoResourceDeliveryMethod.DOWNLOAD -> DeliveryMethod.DOWNLOAD
        VideoResourceDeliveryMethod.STREAM -> DeliveryMethod.STREAM
    }
}
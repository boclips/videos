package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.domain.model.video.DeliveryMethod



data class DeliveryMethodDocument(val deliveryMethod: String)
{
    companion object {
        const val DELIVERY_METHOD_STREAM = "STREAM"
        const val DELIVERY_METHOD_DOWNLOAD = "DOWNLOAD"
    }
}
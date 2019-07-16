package com.boclips.videos.service.infrastructure.video.mongo

data class DistributionMethodDocument(val deliveryMethod: String) {
    companion object {
        const val DELIVERY_METHOD_STREAM = "STREAM"
        const val DELIVERY_METHOD_DOWNLOAD = "DOWNLOAD"
    }
}
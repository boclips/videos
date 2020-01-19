package com.boclips.videos.service.infrastructure.video

data class DistributionMethodDocument(val deliveryMethod: String) {
    companion object {
        const val DELIVERY_METHOD_STREAM = "STREAM"
        const val DELIVERY_METHOD_DOWNLOAD = "DOWNLOAD"
    }
}

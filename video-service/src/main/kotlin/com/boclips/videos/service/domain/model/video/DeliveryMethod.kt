package com.boclips.videos.service.domain.model.video

enum class DeliveryMethod {
    DOWNLOAD,
    STREAM

    ;

    companion object {
        val ALL = setOf(DOWNLOAD, STREAM)
    }
}
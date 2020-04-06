package com.boclips.videos.service.domain.model.video.contentpartner

enum class Availability {
    DOWNLOAD, STREAMING, ALL, NONE;

    fun isDownloadable(): Boolean = this == DOWNLOAD || this == ALL
    fun isStreamable(): Boolean = this == STREAMING || this == ALL
}

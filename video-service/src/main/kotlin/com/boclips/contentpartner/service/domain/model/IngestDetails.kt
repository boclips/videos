package com.boclips.contentpartner.service.domain.model

import com.boclips.videos.api.response.contentpartner.IngestDetailTypes

sealed class IngestDetails {
    abstract fun type(): String
}

object ManualIngest : IngestDetails() {
    override fun type(): String {
        return IngestDetailTypes.MANUAL
    }
}

object CustomIngest : IngestDetails() {
    override fun type(): String {
        return IngestDetailTypes.CUSTOM
    }
}

data class MrssFeedIngest(val url: String) : IngestDetails() {
    override fun type(): String {
        return IngestDetailTypes.MRSS
    }
}

data class YoutubeScrapeIngest(val url: String) : IngestDetails() {
    override fun type(): String {
        return IngestDetailTypes.YOUTUBE
    }
}




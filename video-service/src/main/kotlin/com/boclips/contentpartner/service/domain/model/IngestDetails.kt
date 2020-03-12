package com.boclips.contentpartner.service.domain.model

import com.boclips.videos.api.response.contentpartner.IngestType

sealed class IngestDetails {
    abstract fun type(): IngestType
}

object ManualIngest : IngestDetails() {
    override fun type(): IngestType {
        return IngestType.MANUAL
    }
}

object CustomIngest : IngestDetails() {
    override fun type(): IngestType {
        return IngestType.CUSTOM
    }
}

data class MrssFeedIngest(val url: String) : IngestDetails() {
    override fun type(): IngestType {
        return IngestType.MRSS
    }
}

data class YoutubeScrapeIngest(val url: String) : IngestDetails() {
    override fun type(): IngestType {
        return IngestType.YOUTUBE
    }
}




package com.boclips.contentpartner.service.domain.model

import com.boclips.videos.api.common.IngestType

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

data class MrssFeedIngest(val urls: List<String>) : IngestDetails() {
    override fun type(): IngestType {
        return IngestType.MRSS
    }
}

data class YoutubeScrapeIngest(val playlistIds: List<String>) : IngestDetails() {
    override fun type(): IngestType {
        return IngestType.YOUTUBE
    }
}




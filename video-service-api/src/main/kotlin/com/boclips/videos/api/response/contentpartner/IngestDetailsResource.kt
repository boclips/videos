package com.boclips.videos.api.response.contentpartner

data class IngestDetailsResource(
    val type: IngestType,
    val url: String?
) {
    companion object {
        fun manual() = IngestDetailsResource(type = IngestType.MANUAL, url = null)
        fun custom() = IngestDetailsResource(type = IngestType.CUSTOM, url = null)
        fun mrss(url: String) = IngestDetailsResource(type = IngestType.MRSS, url = url)
        fun youtube(url: String) = IngestDetailsResource(type = IngestType.YOUTUBE, url = url)
    }
}

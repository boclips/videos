package com.boclips.videos.api.response.contentpartner

data class IngestDetailsResource(
    val type: String,
    val url: String?
) {
    companion object {
        fun manual() = IngestDetailsResource(type = IngestDetailTypes.MANUAL, url = null)
        fun custom() = IngestDetailsResource(type = IngestDetailTypes.CUSTOM, url = null)
        fun mrss(url: String) = IngestDetailsResource(type = IngestDetailTypes.MRSS, url = url)
        fun youtube(url: String) = IngestDetailsResource(type = IngestDetailTypes.YOUTUBE, url = url)
    }
}

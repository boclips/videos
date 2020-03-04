package com.boclips.videos.api.response.contentpartner

data class IngestDetailsResource(
    val type: String,
    val url: String?
) {
    companion object {
        const val MANUAL = "MANUAL"
        const val CUSTOM = "CUSTOM"
        const val MRSS = "MRSS"
        const val YOUTUBE = "YOUTUBE"

        fun manual() = IngestDetailsResource(type = MANUAL, url = null)
        fun custom() = IngestDetailsResource(type = CUSTOM, url = null)
        fun mrss(url: String) = IngestDetailsResource(type = MRSS, url = url)
        fun youtube(url: String) = IngestDetailsResource(type = YOUTUBE, url = url)
    }
}

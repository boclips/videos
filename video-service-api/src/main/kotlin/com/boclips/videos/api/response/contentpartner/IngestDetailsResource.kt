package com.boclips.videos.api.response.contentpartner

data class IngestDetailsResource(
    val type: IngestType,
    val playlistIds: List<String>? = null,
    val urls: List<String>? = null
) {
    companion object {
        fun manual() = IngestDetailsResource(type = IngestType.MANUAL)
        fun custom() = IngestDetailsResource(type = IngestType.CUSTOM)
        fun mrss(vararg urls: String) = mrss(urls.toList())
        fun mrss(urls: List<String>) = IngestDetailsResource(type = IngestType.MRSS, urls = urls)
        fun youtube(vararg playlistIds: String) = youtube(playlistIds.toList())
        fun youtube(playlistIds: List<String>) = IngestDetailsResource(type = IngestType.YOUTUBE, playlistIds = playlistIds)
    }
}

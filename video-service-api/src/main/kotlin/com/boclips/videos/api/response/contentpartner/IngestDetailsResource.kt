package com.boclips.videos.api.response.contentpartner

data class IngestDetailsResource(
    val type: IngestType,
    val urls: List<String>?
) {
    companion object {
        fun manual() = IngestDetailsResource(type = IngestType.MANUAL, urls = null)
        fun custom() = IngestDetailsResource(type = IngestType.CUSTOM, urls = null)
        fun mrss(vararg urls: String) = mrss(urls.toList())
        fun mrss(urls: List<String>) = IngestDetailsResource(type = IngestType.MRSS, urls = urls)
        fun youtube(vararg urls: String) = youtube(urls.toList())
        fun youtube(urls: List<String>) = IngestDetailsResource(type = IngestType.YOUTUBE, urls = urls)
    }
}

package com.boclips.videos.service.domain.model.video

data class Caption (
    val content: String,
    val format: CaptionFormat
)

enum class CaptionFormat {
    SRT,
    DFXP,
    WEBVTT,
    CAP;

    fun getFileExtension() : String = when(this) {
        SRT -> "srt"
        DFXP -> "dfxp"
        WEBVTT -> "vtt"
        CAP -> "cap"
    }
}
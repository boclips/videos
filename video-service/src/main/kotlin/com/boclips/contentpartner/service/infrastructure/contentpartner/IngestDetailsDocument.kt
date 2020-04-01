package com.boclips.contentpartner.service.infrastructure.contentpartner

data class IngestDetailsDocument(
    val type: String,
    val playlistIds: List<String>? = null,
    val urls: List<String>? = null
)

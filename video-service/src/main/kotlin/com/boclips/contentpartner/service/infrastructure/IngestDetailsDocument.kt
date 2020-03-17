package com.boclips.contentpartner.service.infrastructure

data class IngestDetailsDocument(
    val type: String,
    val playlistIds: List<String>? = null,
    val urls: List<String>? = null
)

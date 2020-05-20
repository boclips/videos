package com.boclips.contentpartner.service.infrastructure.channel

data class IngestDetailsDocument(
    val type: String,
    val playlistIds: List<String>? = null,
    val urls: List<String>? = null
)

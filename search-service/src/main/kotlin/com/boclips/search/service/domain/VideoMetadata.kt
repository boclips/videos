package com.boclips.search.service.domain

data class VideoMetadata(
    val id: String,
    val title: String,
    val description: String,
    val contentProvider: String,
    val keywords: List<String>,
    val tags: List<String>
)

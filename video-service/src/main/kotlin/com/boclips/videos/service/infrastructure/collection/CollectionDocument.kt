package com.boclips.videos.service.infrastructure.collection

import java.time.Instant

data class CollectionDocument(
    val id: String,
    val owner: String,
    val title: String,
    val videos: List<String>,
    val updatedAt: Instant
)

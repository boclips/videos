package com.boclips.videos.service.infrastructure.video

data class TopicDocument(
    val name: String,
    val language: String,
    val confidence: Double,
    val parent: TopicDocument?
)
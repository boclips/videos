package com.boclips.videos.service.infrastructure.video.mongo

data class TopicDocument(
        val name: String,
        val language: String,
        val confidence: Double,
        val parent: TopicDocument?
    )
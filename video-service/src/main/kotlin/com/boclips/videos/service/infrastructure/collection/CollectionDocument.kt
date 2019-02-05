package com.boclips.videos.service.infrastructure.collection

data class CollectionDocument(
        val id: String,
        val owner: String,
        val title: String?,
        val videos: List<String>)

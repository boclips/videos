package com.boclips.videos.service.infrastructure.collection.mongo

data class CollectionDocument(
        val id: String,
        val owner: String,
        val title: String?,
        val videos: List<String>)

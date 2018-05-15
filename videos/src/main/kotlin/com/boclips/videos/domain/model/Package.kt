package com.boclips.videos.domain.model

data class Package(
        val id: String,
        val name: String,
        val excludedContentProviders: List<ContentProvider> = emptyList()
)

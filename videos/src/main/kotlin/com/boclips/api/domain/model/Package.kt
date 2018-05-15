package com.boclips.api.domain.model

data class Package(
        val id: String,
        val name: String,
        val excludedContentProviders: List<ContentProvider> = emptyList()
)

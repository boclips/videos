package com.boclips.api

data class Package(
        val id: String,
        val name: String,
        val excludedContentProviders: List<String> = emptyList()
)

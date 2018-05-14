package com.boclips.api

import com.boclips.api.contentproviders.ContentProvider

data class Package(
        val id: String,
        val name: String,
        val excludedContentProviders: List<ContentProvider> = emptyList()
)

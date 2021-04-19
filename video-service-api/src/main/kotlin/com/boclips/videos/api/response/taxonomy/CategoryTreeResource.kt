package com.boclips.videos.api.response.taxonomy

data class CategoryTreeResource(
    val description: String,
    val children: Map<String, CategoryTreeResource>
)

package com.boclips.videos.api.response.taxonomy

data class CategoryResource(
    val _embedded: CategoryResourceWrapper
)

typealias CategoryResourceWrapper = Map<String, CategoryTreeResource>

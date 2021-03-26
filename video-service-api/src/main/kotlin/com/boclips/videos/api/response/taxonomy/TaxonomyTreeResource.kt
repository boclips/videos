package com.boclips.videos.api.response.taxonomy

data class TaxonomyTreeResource(
    val description: String,
    val children: Map<String, TaxonomyTreeResource>
)

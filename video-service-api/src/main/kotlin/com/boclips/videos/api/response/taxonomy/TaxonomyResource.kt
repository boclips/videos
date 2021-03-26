package com.boclips.videos.api.response.taxonomy

data class TaxonomyResource(
    val _embedded: TaxonomyResourceWrapper
)

typealias TaxonomyResourceWrapper = Map<String, TaxonomyTreeResource>

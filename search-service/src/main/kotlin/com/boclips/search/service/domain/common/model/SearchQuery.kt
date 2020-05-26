package com.boclips.search.service.domain.common.model

abstract class SearchQuery<METADATA>(
    val phrase: String = "",
    val sort: List<Sort<METADATA>> = emptyList(),
    open val facetDefinition: FacetDefinition? = null
)

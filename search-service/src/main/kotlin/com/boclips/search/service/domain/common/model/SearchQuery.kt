package com.boclips.search.service.domain.common.model

abstract class SearchQuery<METADATA>(
    open val phrase: String = "",
    open val sort: List<Sort<METADATA>> = emptyList(),
    open val facetDefinition: FacetDefinition? = null
)

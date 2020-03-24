package com.boclips.search.service.domain.common

class ResultCounts(val totalHits: Long, val facets: List<FacetCount>? = null) {
    fun getFacetCounts(facetType: FacetType): List<Count> {
        return facets?.find { it.type == facetType }?.counts ?: emptyList()
    }
}

class FacetCount(val type: FacetType, val counts: List<Count>)

data class Count(val id: String, val hits: Long)

sealed class FacetType {
    object Subjects : FacetType()
    object AgeRanges : FacetType()
}

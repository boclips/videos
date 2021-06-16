package com.boclips.search.service.domain.common

class ResultCounts(val totalHits: Long, val facets: List<FacetCount>? = null) {
    fun getFacetCounts(facetType: FacetType): List<Count> {
        return facets?.find { it.type == facetType }?.counts?.toList() ?: emptyList()
    }
}

class FacetCount(val type: FacetType, val counts: Set<Count>)

data class Count(val id: String, val hits: Long)

sealed class FacetType {
    object Subjects : FacetType()
    object AgeRanges : FacetType()
    object Duration : FacetType()
    object AttachmentTypes : FacetType()
    object Channels : FacetType()
    object VideoTypes : FacetType()
    object Prices : FacetType()
}

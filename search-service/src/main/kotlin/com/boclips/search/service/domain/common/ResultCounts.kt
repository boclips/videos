package com.boclips.search.service.domain.common

class ResultCounts(val totalHits: Long, val facets: List<FacetCount>? = null) {
    fun getFacetCounts(facet: Facet): List<Count> {
        return when (facet) {
            is Facet.SubjectsFacet -> {
                facets?.find { it.key is Facet.SubjectsFacet }?.counts ?: emptyList()
            }
        }
    }
}

class FacetCount(val key: Facet, val counts: List<Count>)

data class Count(val id: String, val hits: Long)

sealed class Facet {
    object SubjectsFacet : Facet()
}

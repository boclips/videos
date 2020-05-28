package com.boclips.search.service.domain.collections.model

import com.boclips.search.service.domain.common.model.FacetDefinition
import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.videos.model.AgeRange

class CollectionQuery(
    phrase: String = "",
    sort: List<Sort<CollectionMetadata>> = emptyList(),
    facetDefinition: FacetDefinition.Collection? = null,
    val searchable: Boolean? = null,
    val owner: String? = null,
    val subjectIds: List<String> = emptyList(),
    val permittedIds: List<String>? = null,
    val bookmarkedBy: String? = null,
    val hasLessonPlans: Boolean? = null,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null,
    val ageRanges: List<AgeRange>? = null,
    val promoted: Boolean? = null,
    val resourceTypes: Set<String> = emptySet()
) : SearchQuery<CollectionMetadata>(phrase, sort, facetDefinition)

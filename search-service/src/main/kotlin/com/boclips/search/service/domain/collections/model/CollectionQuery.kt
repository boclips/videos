package com.boclips.search.service.domain.collections.model

import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.Sort

class CollectionQuery(
    phrase: String = "",
    val visibility: List<CollectionVisibility> = listOf(CollectionVisibility.PUBLIC),
    sort: Sort<CollectionMetadata>? = null,
    val subjectIds: List<String> = emptyList()
) : SearchQuery<CollectionMetadata>(phrase, sort)

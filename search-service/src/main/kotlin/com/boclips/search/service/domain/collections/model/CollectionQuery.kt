package com.boclips.search.service.domain.collections.model

import com.boclips.search.service.domain.model.SearchQuery
import com.boclips.search.service.domain.model.Sort

class CollectionQuery (
    phrase: String? = null,
    sort: Sort<CollectionMetadata>? = null
) : SearchQuery<CollectionMetadata>(phrase, sort)

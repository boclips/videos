package com.boclips.videos.service.domain.service.collection

import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery

interface CollectionSearchService : ReadSearchService<CollectionMetadata, CollectionQuery>,
    WriteSearchService<com.boclips.videos.service.domain.model.collection.Collection>
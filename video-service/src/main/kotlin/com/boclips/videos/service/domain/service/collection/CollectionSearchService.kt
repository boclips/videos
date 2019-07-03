package com.boclips.videos.service.domain.service.collection

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.videos.service.domain.model.collection.Collection

interface CollectionSearchService : IndexReader<CollectionMetadata, CollectionQuery>, IndexWriter<Collection>
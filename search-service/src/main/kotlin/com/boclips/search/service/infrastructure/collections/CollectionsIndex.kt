package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.infrastructure.Index

object CollectionsIndex : Index {

    private const val ES_COLLECTIONS_INDEX_PREFIX = "collections_"
    private const val ES_COLLECTIONS_INDEX_ALIAS = "current_collections"
    private const val ES_COLLECTIONS_INDEX_WILDCARD = "$ES_COLLECTIONS_INDEX_PREFIX*"

    override fun getESType() = "collection"
    override fun getIndexAlias() = ES_COLLECTIONS_INDEX_ALIAS
    override fun getIndexWildcard() = ES_COLLECTIONS_INDEX_WILDCARD
    override fun generateIndexName() = Index.generateIndexName(ES_COLLECTIONS_INDEX_PREFIX)
}

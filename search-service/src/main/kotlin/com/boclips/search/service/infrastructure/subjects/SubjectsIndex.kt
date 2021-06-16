package com.boclips.search.service.infrastructure.subjects

import com.boclips.search.service.infrastructure.Index

object SubjectsIndex : Index {

    private const val ES_CHANNELS_INDEX_PREFIX = "subjects_"
    private const val ES_CHANNELS_INDEX_ALIAS = "current_subjects"
    private const val ES_CHANNELS_INDEX_WILDCARD = "$ES_CHANNELS_INDEX_PREFIX*"

    override fun getESType() = "subject"
    override fun getIndexAlias() = ES_CHANNELS_INDEX_ALIAS
    override fun getIndexWildcard() = ES_CHANNELS_INDEX_WILDCARD
    override fun generateIndexName() = Index.generateIndexName(ES_CHANNELS_INDEX_PREFIX)
}

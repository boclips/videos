package com.boclips.search.service.domain.common

interface IndexWriter<T> {
    fun safeRebuildIndex(items: Sequence<T>, notifier: ProgressNotifier? = null)
    fun upsert(items: Sequence<T>, notifier: ProgressNotifier? = null)
    fun removeFromSearch(itemId: String)
    fun bulkRemoveFromSearch(itemIds: List<String>)
    fun makeSureIndexIsThere()
}

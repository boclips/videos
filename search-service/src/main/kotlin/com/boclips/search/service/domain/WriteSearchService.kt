package com.boclips.search.service.domain

interface WriteSearchService<T> {
    fun safeRebuildIndex(items: Sequence<T>, notifier: ProgressNotifier? = null)
    fun upsert(items: Sequence<T>, notifier: ProgressNotifier? = null)
    fun removeFromSearch(itemId: String)
    fun bulkRemoveFromSearch(items: List<String>)
}

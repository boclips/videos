package com.boclips.search.service.domain

interface AdminSearchService<T> {
    fun safeRebuildIndex(items: Sequence<T>, notifier: ProgressNotifier? = null)
    fun upsert(items: Sequence<T>, notifier: ProgressNotifier? = null)
    fun removeFromSearch(itemId: String)
}
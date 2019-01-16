package com.boclips.search.service.domain

interface ProgressNotifier {
    fun send(message: String)
    fun complete() = send("OPERATION COMPLETE")
}

interface GenericSearchServiceAdmin<T> {
    fun safeRebuildIndex(videos: Sequence<T>, notifier: ProgressNotifier? = null)
    fun upsert(videos: Sequence<T>, notifier: ProgressNotifier? = null)
    fun removeFromSearch(videoId: String)
}
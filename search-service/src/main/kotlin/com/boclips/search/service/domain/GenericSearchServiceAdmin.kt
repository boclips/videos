package com.boclips.search.service.domain

interface GenericSearchServiceAdmin<T> {
    fun safeRebuildIndex(videos: Sequence<T>)
    fun upsert(videos: Sequence<T>)
    fun removeFromSearch(videoId: String)
}
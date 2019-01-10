package com.boclips.search.service.domain

interface GenericSearchServiceAdmin<T> {
    fun resetIndex()
    fun upsert(videos: Sequence<T>)
    fun removeFromSearch(videoId: String)
}
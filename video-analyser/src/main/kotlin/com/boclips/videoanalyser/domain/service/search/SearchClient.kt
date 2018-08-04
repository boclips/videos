package com.boclips.videoanalyser.domain.service.search

interface SearchClient {
    fun searchTop10(query: String): Iterable<String>
}
package com.boclips.videoanalyser.domain.search_benchmark.service

interface SearchClient {
    fun searchTop10(query: String) : Iterable<String>
}
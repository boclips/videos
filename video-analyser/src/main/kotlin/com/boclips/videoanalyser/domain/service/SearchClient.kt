package com.boclips.videoanalyser.domain.service

interface SearchClient {
    fun searchTop10(query: String) : Iterable<String>
}
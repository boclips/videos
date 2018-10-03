package com.boclips.videoanalyser.domain.service.search

interface SearchClient {

    fun serviceName(): String

    fun searchTop10(query: String): Iterable<String>
}
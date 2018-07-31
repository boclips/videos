package com.boclips.videoanalyser.domain.search_benchmark.service

import com.boclips.videoanalyser.domain.search_benchmark.domain.SearchExpectation
import java.io.File

interface SearchExpectationsParser {

    fun parseCsvFile(file: File): Iterable<SearchExpectation>

}
package com.boclips.videoanalyser.domain.service

import java.io.File

interface SearchExpectationsParser {

    fun parseCsvFile(file: File): Iterable<SearchExpectation>

}
package com.boclips.videoanalyser.domain.model.search

data class SearchBenchmarkReportItem(val expectation: SearchExpectation, val legacySearchHit: Boolean, val videoServiceHit: Boolean)
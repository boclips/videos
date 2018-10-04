package com.boclips.videoanalyser.application.csv

import com.fasterxml.jackson.annotation.JsonProperty

data class SearchBenchmarkReportItemCsv(
    @JsonProperty(value = "Query")
    var query: String? = null,
    @JsonProperty(value = "Video")
    var video: String? = null,
    @JsonProperty(value = "boclips.com")
    var legacySearchHit: String? = null,
    @JsonProperty(value = "video-service")
    var videoServiceHit: String? = null
)
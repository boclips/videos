package com.boclips.videoanalyser.presentation

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.domain.model.KalturaVideo
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class BoclipsVideoCsv(
        @JsonProperty(value = "Id")
        var id: String? = null,
        @JsonProperty(value = "Reference Id")
        var referenceId: String? = null,
        @JsonProperty(value = "Title")
        var title: String? = null,
        @JsonProperty(value = "Content Provider")
        var provider: String? = null,
        @JsonProperty(value = "Description")
        var description: String? = null,
        @JsonProperty(value = "Date")
        var date: LocalDateTime? = null,
        @JsonProperty(value = "Duration")
        var duration: String? = null
) {
    companion object {
        fun from(video: BoclipsVideo) = BoclipsVideoCsv().apply {
            id = video.id
            referenceId = video.referenceId
            title = video.title
            provider = video.contentProvider
            description = video.description
            date = video.date
            duration = video.duration
        }

        fun from(video: KalturaVideo) = BoclipsVideoCsv().apply {
            id = video.id
            referenceId = video.referenceId
        }
    }
}

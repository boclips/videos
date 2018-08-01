package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.domain.common.model.BoclipsVideo
import com.boclips.videoanalyser.domain.common.model.KalturaVideo
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class BoclipsVideoCsv(
        @JsonProperty(value = ID)
        var id: String? = null,
        @JsonProperty(value = REFERENCE_ID)
        var referenceId: String? = null,
        @JsonProperty(value = TITLE)
        var title: String? = null,
        @JsonProperty(value = CONTENT_PROVIDER)
        var provider: String? = null,
        @JsonProperty(value = CONTENT_PROVIDER_ID)
        var providerId: String? = null,
        @JsonProperty(value = DESCRIPTION)
        var description: String? = null,
        @JsonProperty(value = DATE)
        var date: LocalDateTime? = null,
        @JsonProperty(value = DURATION)
        var duration: String? = null,
        @JsonProperty(value = NOTES)
        var notes: String? = null
) {
    companion object {
        fun from(video: BoclipsVideo, notes: String? = null) = BoclipsVideoCsv().apply {
            id = "${video.id}"
            referenceId = video.referenceId
            title = video.title
            provider = video.contentProvider
            providerId = video.contentProviderId
            description = video.description
            date = video.date
            duration = video.duration
            this.notes = notes
        }

        fun from(video: KalturaVideo) = BoclipsVideoCsv().apply {
            id = video.id
            referenceId = video.referenceId
        }

        const val ID = "Id"
        const val REFERENCE_ID = "Reference Id"
        const val TITLE = "Title"
        const val CONTENT_PROVIDER = "Content Provider"
        const val CONTENT_PROVIDER_ID = "Content Provider Id"
        const val DESCRIPTION = "Description"
        const val DATE = "Date"
        const val DURATION = "Duration"
        const val NOTES = "Notes"

        val ALL_COLUMNS = setOf(ID, REFERENCE_ID, TITLE, CONTENT_PROVIDER, CONTENT_PROVIDER_ID, DESCRIPTION, DATE, DURATION, NOTES)
    }

}

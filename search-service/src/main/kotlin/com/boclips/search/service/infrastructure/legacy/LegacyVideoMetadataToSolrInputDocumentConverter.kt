package com.boclips.search.service.infrastructure.legacy

import com.boclips.search.service.domain.legacy.LegacyVideoMetadata
import org.apache.solr.common.SolrInputDocument
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object LegacyVideoMetadataToSolrInputDocumentConverter {

    fun convert(video: LegacyVideoMetadata): SolrInputDocument {
        return SolrInputDocument().apply {
            addField("id", video.id)
            addField("title", video.title)
            addField("description", video.description)
            addField("keywords", video.keywords.joinToString(separator = ", "))
            addField("duration", LegacyDurationFormatter.format(video.duration))
            addField("durationsecs", video.duration.seconds.toInt())
            addField(
                "clip_date",
                video.releaseDate.atStartOfDay().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            )
            addField("source", video.contentPartnerName)
            addField("unique_id", video.contentPartnerVideoId)
            addField("namespace", "${video.contentPartnerName}:${video.contentPartnerVideoId}")
            addField("typename", video.videoTypeTitle)
        }
    }
}
package com.boclips.contentpartner.service.infrastructure.contentpartner.converters

import com.boclips.contentpartner.service.domain.model.contentpartner.CustomIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.IngestDetails
import com.boclips.contentpartner.service.domain.model.contentpartner.ManualIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.YoutubeScrapeIngest
import com.boclips.contentpartner.service.infrastructure.contentpartner.IngestDetailsDocument

object IngestDetailsDocumentConverter {

    private const val MANUAL = "MANUAL"
    private const val CUSTOM = "CUSTOM"
    private const val MRSS = "MRSS"
    private const val YOUTUBE = "YOUTUBE"

    fun toIngestDetailsDocument(ingestDetails: IngestDetails): IngestDetailsDocument {
        return when(ingestDetails) {
            ManualIngest -> IngestDetailsDocument(
                type = MANUAL
            )
            CustomIngest -> IngestDetailsDocument(
                type = CUSTOM
            )
            is MrssFeedIngest -> IngestDetailsDocument(
                type = MRSS,
                urls = ingestDetails.urls
            )
            is YoutubeScrapeIngest -> IngestDetailsDocument(
                type = YOUTUBE,
                playlistIds = ingestDetails.playlistIds
            )
        }
    }

    fun toIngestDetails(document: IngestDetailsDocument): IngestDetails {
        return when(document.type) {
            MANUAL -> ManualIngest
            CUSTOM -> CustomIngest
            MRSS -> MrssFeedIngest(
                urls = document.urls!!
            )
            YOUTUBE -> YoutubeScrapeIngest(
                playlistIds = document.playlistIds ?: document.urls!!
            )
            else -> throw IllegalArgumentException("Cannot convert ingest details document: $document")
        }
    }
}

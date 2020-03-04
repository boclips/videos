package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.CustomIngest
import com.boclips.contentpartner.service.domain.model.IngestDetails
import com.boclips.contentpartner.service.domain.model.ManualIngest
import com.boclips.contentpartner.service.domain.model.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.YoutubeScrapeIngest

object IngestDetailsDocumentConverter {

    private const val MANUAL = "MANUAL"
    private const val CUSTOM = "CUSTOM"
    private const val MRSS = "MRSS"
    private const val YOUTUBE = "YOUTUBE"

    fun toIngestDetailsDocument(ingestDetails: IngestDetails): IngestDetailsDocument {
        return when(ingestDetails) {
            ManualIngest -> IngestDetailsDocument(type = MANUAL, url = null)
            CustomIngest -> IngestDetailsDocument(type = CUSTOM, url = null)
            is MrssFeedIngest -> IngestDetailsDocument(type = MRSS, url = ingestDetails.url)
            is YoutubeScrapeIngest -> IngestDetailsDocument(type = YOUTUBE, url = ingestDetails.url)
        }
    }

    fun toIngestDetails(document: IngestDetailsDocument): IngestDetails {
        return when(document.type) {
            MANUAL -> ManualIngest
            CUSTOM -> CustomIngest
            MRSS -> MrssFeedIngest(url = document.url!!)
            YOUTUBE -> YoutubeScrapeIngest(url = document.url!!)
            else -> throw IllegalArgumentException("Cannot convert ingest details document: $document")
        }
    }
}

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
            ManualIngest -> IngestDetailsDocument(type = MANUAL, urls = null)
            CustomIngest -> IngestDetailsDocument(type = CUSTOM, urls = null)
            is MrssFeedIngest -> IngestDetailsDocument(type = MRSS, urls = ingestDetails.urls)
            is YoutubeScrapeIngest -> IngestDetailsDocument(type = YOUTUBE, urls = ingestDetails.urls)
        }
    }

    fun toIngestDetails(document: IngestDetailsDocument): IngestDetails {
        return when(document.type) {
            MANUAL -> ManualIngest
            CUSTOM -> CustomIngest
            MRSS -> MrssFeedIngest(urls = document.urls!!)
            YOUTUBE -> YoutubeScrapeIngest(urls = document.urls!!)
            else -> throw IllegalArgumentException("Cannot convert ingest details document: $document")
        }
    }
}

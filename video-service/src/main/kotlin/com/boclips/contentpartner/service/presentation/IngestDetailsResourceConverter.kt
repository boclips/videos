package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.CustomIngest
import com.boclips.contentpartner.service.domain.model.IngestDetails
import com.boclips.contentpartner.service.domain.model.ManualIngest
import com.boclips.contentpartner.service.domain.model.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.YoutubeScrapeIngest
import com.boclips.videos.api.response.contentpartner.IngestDetailsResource
import com.boclips.videos.api.response.contentpartner.IngestType

class IngestDetailsResourceConverter {

    fun convert(ingestDetails: IngestDetails): IngestDetailsResource {
        return when(ingestDetails) {
            ManualIngest -> IngestDetailsResource.manual()
            CustomIngest -> IngestDetailsResource.custom()
            is MrssFeedIngest -> IngestDetailsResource.mrss(ingestDetails.urls)
            is YoutubeScrapeIngest -> IngestDetailsResource.youtube(ingestDetails.urls)
        }
    }

    fun fromResource(ingestDetailsResource: IngestDetailsResource): IngestDetails {
        return when(ingestDetailsResource.type) {
            IngestType.MANUAL -> ManualIngest
            IngestType.CUSTOM -> CustomIngest
            IngestType.MRSS -> MrssFeedIngest(ingestDetailsResource.urls!!)
            IngestType.YOUTUBE -> YoutubeScrapeIngest(ingestDetailsResource.urls!!)
            else -> throw IllegalArgumentException("Unknown ingest details type: ${ingestDetailsResource.type}")
        }
    }
}

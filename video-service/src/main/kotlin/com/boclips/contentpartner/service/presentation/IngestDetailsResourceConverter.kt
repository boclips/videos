package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.CustomIngest
import com.boclips.contentpartner.service.domain.model.IngestDetails
import com.boclips.contentpartner.service.domain.model.ManualIngest
import com.boclips.contentpartner.service.domain.model.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.YoutubeScrapeIngest
import com.boclips.videos.api.response.contentpartner.IngestDetailsResource

class IngestDetailsResourceConverter {

    fun convert(ingestDetails: IngestDetails): IngestDetailsResource {
        return when(ingestDetails) {
            ManualIngest -> IngestDetailsResource.manual()
            CustomIngest -> IngestDetailsResource.custom()
            is MrssFeedIngest -> IngestDetailsResource.mrss(ingestDetails.url)
            is YoutubeScrapeIngest -> IngestDetailsResource.youtube(ingestDetails.url)
        }
    }

    fun fromResource(ingestDetailsResource: IngestDetailsResource): IngestDetails {
        return when(ingestDetailsResource.type) {
            IngestDetailsResource.MANUAL -> ManualIngest
            IngestDetailsResource.CUSTOM -> CustomIngest
            IngestDetailsResource.MRSS -> MrssFeedIngest(ingestDetailsResource.url!!)
            IngestDetailsResource.YOUTUBE -> YoutubeScrapeIngest(ingestDetailsResource.url!!)
            else -> throw IllegalArgumentException("Unknown ingest details type: ${ingestDetailsResource.type}")
        }
    }
}

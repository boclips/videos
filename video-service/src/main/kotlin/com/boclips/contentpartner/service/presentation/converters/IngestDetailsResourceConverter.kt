package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.channel.CustomIngest
import com.boclips.contentpartner.service.domain.model.channel.IngestDetails
import com.boclips.contentpartner.service.domain.model.channel.ManualIngest
import com.boclips.contentpartner.service.domain.model.channel.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.channel.YoutubeScrapeIngest
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.api.response.contentpartner.IngestDetailsResource

class IngestDetailsResourceConverter {

    fun convert(ingestDetails: IngestDetails): IngestDetailsResource {
        return when (ingestDetails) {
            ManualIngest -> IngestDetailsResource.manual()
            CustomIngest -> IngestDetailsResource.custom()
            is MrssFeedIngest -> IngestDetailsResource.mrss(ingestDetails.urls)
            is YoutubeScrapeIngest -> IngestDetailsResource.youtube(ingestDetails.playlistIds)
        }
    }

    fun fromResource(ingestDetailsResource: IngestDetailsResource): IngestDetails {
        return when (ingestDetailsResource.type) {
            IngestType.MANUAL -> ManualIngest
            IngestType.CUSTOM -> CustomIngest
            IngestType.MRSS -> MrssFeedIngest(
                ingestDetailsResource.urls!!
            )
            IngestType.YOUTUBE -> YoutubeScrapeIngest(
                ingestDetailsResource.playlistIds ?: ingestDetailsResource.urls!!
            )
        }
    }
}

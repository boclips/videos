package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.CustomIngest
import com.boclips.contentpartner.service.domain.model.IngestDetails
import com.boclips.contentpartner.service.domain.model.ManualIngest
import com.boclips.contentpartner.service.domain.model.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.YoutubeScrapeIngest
import com.boclips.videos.api.response.contentpartner.IngestDetailsResource

class IngestDetailsToResourceConverter {

    fun convert(ingestDetails: IngestDetails): IngestDetailsResource {
        return when(ingestDetails) {
            ManualIngest -> IngestDetailsResource.manual()
            CustomIngest -> IngestDetailsResource.custom()
            is MrssFeedIngest -> IngestDetailsResource.mrss(ingestDetails.url)
            is YoutubeScrapeIngest -> IngestDetailsResource.youtube(ingestDetails.url)
        }
    }
}

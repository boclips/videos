package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.ContentPartnerMarketingInformation
import com.boclips.videos.api.common.ExplicitlyNull
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.response.contentpartner.ContentPartnerMarketingResource

object MarketingInformationToResourceConverter {
    fun from(marketingInformation: ContentPartnerMarketingInformation?) =
        marketingInformation?.let {
            ContentPartnerMarketingResource(
                status = it.status?.name,
                logos = it.logos?.map { logoUrl -> logoUrl.toString() },
                showreel = it.showreel?.let { showreel ->
                    when (showreel) {
                        is Specified -> showreel.value.toString()
                        is ExplicitlyNull -> null
                    }
                },
                sampleVideos = it.sampleVideos?.map { sampleVideoUrl -> sampleVideoUrl.toString() }
            )
        }
}

package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.channel.MarketingInformation
import com.boclips.videos.api.response.channel.MarketingResource

object MarketingInformationToResourceConverter {
    fun from(marketingInformation: MarketingInformation?) =
        marketingInformation?.let {
            MarketingResource(
                status = it.status?.name,
                logos = it.logos?.map { logoUrl -> logoUrl.toString() },
                showreel = it.showreel?.toString(),
                sampleVideos = it.sampleVideos?.map { sampleVideoUrl -> sampleVideoUrl.toString() }
            )
        }
}

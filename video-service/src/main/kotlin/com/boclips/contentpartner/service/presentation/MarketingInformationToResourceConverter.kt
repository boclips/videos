package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.MarketingInformation
import com.boclips.videos.api.response.contentpartner.ContentPartnerMarketingResource

object MarketingInformationToResourceConverter {
    fun from(marketingInformation: MarketingInformation?) =
        marketingInformation?.status?.let {
            ContentPartnerMarketingResource(
                status = it.name
            )
        }
}
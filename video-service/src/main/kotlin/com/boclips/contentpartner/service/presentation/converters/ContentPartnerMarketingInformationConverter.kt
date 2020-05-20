package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.channel.MarketingInformation
import com.boclips.videos.api.request.contentpartner.ContentPartnerRequest

object ContentPartnerMarketingInformationConverter {
    fun convert(contentPartnerRequest: ContentPartnerRequest): MarketingInformation =
        MarketingInformation(
            oneLineDescription = contentPartnerRequest.oneLineDescription,
            status = contentPartnerRequest.marketingInformation?.status?.let(ContentPartnerMarketingStatusConverter::convert),
            logos = contentPartnerRequest.marketingInformation?.logos?.map(UrlConverter::convert),
            showreel = contentPartnerRequest.marketingInformation?.showreel?.map(UrlConverter::convert)?.orNull(),
            sampleVideos = contentPartnerRequest.marketingInformation?.sampleVideos?.map(UrlConverter::convert)
        )
}

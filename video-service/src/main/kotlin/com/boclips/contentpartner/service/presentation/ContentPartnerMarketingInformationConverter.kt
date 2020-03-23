package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.ContentPartnerMarketingInformation
import com.boclips.videos.api.request.contentpartner.ContentPartnerRequest

object ContentPartnerMarketingInformationConverter {
    fun convert(contentPartnerRequest: ContentPartnerRequest): ContentPartnerMarketingInformation =
        ContentPartnerMarketingInformation(
            oneLineDescription = contentPartnerRequest.oneLineDescription,
            status = contentPartnerRequest.marketingInformation?.status?.let(ContentPartnerMarketingStatusConverter::convert),
            logos = contentPartnerRequest.marketingInformation?.logos?.map(UrlConverter::convert),
            showreel = contentPartnerRequest.marketingInformation?.showreel?.map(UrlConverter::convert),
            sampleVideos = contentPartnerRequest.marketingInformation?.sampleVideos?.map(UrlConverter::convert)
        )
}

package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.ContentPartnerMarketingInformation
import com.boclips.videos.api.request.contentpartner.UpsertContentPartnerRequest

object ContentPartnerMarketingInformationConverter {
    fun convert(contentPartnerRequest: UpsertContentPartnerRequest): ContentPartnerMarketingInformation =
        ContentPartnerMarketingInformation(
            oneLineDescription = contentPartnerRequest.oneLineDescription,
            status = contentPartnerRequest.marketingInformation?.status?.let(ContentPartnerMarketingStatusConverter::convert),
            logos = contentPartnerRequest.marketingInformation?.logos?.map(ContentPartnerUrlConverter::convert),
            showreel = contentPartnerRequest.marketingInformation?.showreel?.map(ContentPartnerUrlConverter::convert),
            sampleVideos = contentPartnerRequest.marketingInformation?.sampleVideos?.map(ContentPartnerUrlConverter::convert)
        )
}

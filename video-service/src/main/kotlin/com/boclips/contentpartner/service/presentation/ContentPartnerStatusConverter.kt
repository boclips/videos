package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.ContentPartnerStatus
import com.boclips.videos.api.request.contentpartner.ContentPartnerStatusRequest

object ContentPartnerStatusConverter {
    fun convert(marketingStatusRequest: ContentPartnerStatusRequest): ContentPartnerStatus =
        when (marketingStatusRequest) {
            ContentPartnerStatusRequest.NeedsIntroduction -> ContentPartnerStatus.NeedsIntroduction
            ContentPartnerStatusRequest.HaveReachedOut -> ContentPartnerStatus.HaveReachedOut
            ContentPartnerStatusRequest.NeedsContent -> ContentPartnerStatus.NeedsContent
            ContentPartnerStatusRequest.WaitingForIngest -> ContentPartnerStatus.WaitingForIngest
            ContentPartnerStatusRequest.ShouldAddToSite -> ContentPartnerStatus.ShouldAddToSite
            ContentPartnerStatusRequest.ShouldPromote -> ContentPartnerStatus.ShouldPromote
            ContentPartnerStatusRequest.Promoted -> ContentPartnerStatus.Promoted
        }
}

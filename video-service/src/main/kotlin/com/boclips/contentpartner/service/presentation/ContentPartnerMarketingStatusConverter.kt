package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.ContentPartnerStatus
import com.boclips.videos.api.request.contentpartner.ContentPartnerStatusRequest

object ContentPartnerMarketingStatusConverter {
    fun convert(statusRequest: ContentPartnerStatusRequest) =
        when (statusRequest) {
            ContentPartnerStatusRequest.NEEDS_INTRODUCTION -> ContentPartnerStatus.NEEDS_INTRODUCTION
            ContentPartnerStatusRequest.HAVE_REACHED_OUT -> ContentPartnerStatus.HAVE_REACHED_OUT
            ContentPartnerStatusRequest.NEEDS_CONTENT -> ContentPartnerStatus.NEEDS_CONTENT
            ContentPartnerStatusRequest.WAITING_FOR_INGEST -> ContentPartnerStatus.WAITING_FOR_INGEST
            ContentPartnerStatusRequest.SHOULD_ADD_TO_SITE -> ContentPartnerStatus.SHOULD_ADD_TO_SITE
            ContentPartnerStatusRequest.SHOULD_PROMOTE -> ContentPartnerStatus.SHOULD_PROMOTE
            ContentPartnerStatusRequest.PROMOTED -> ContentPartnerStatus.PROMOTED
        }
}

package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.channel.ChannelStatus
import com.boclips.videos.api.request.contentpartner.ContentPartnerStatusRequest

object ContentPartnerMarketingStatusConverter {
    fun convert(statusRequest: ContentPartnerStatusRequest) =
        when (statusRequest) {
            ContentPartnerStatusRequest.NEEDS_INTRODUCTION -> ChannelStatus.NEEDS_INTRODUCTION
            ContentPartnerStatusRequest.HAVE_REACHED_OUT -> ChannelStatus.HAVE_REACHED_OUT
            ContentPartnerStatusRequest.NEEDS_CONTENT -> ChannelStatus.NEEDS_CONTENT
            ContentPartnerStatusRequest.WAITING_FOR_INGEST -> ChannelStatus.WAITING_FOR_INGEST
            ContentPartnerStatusRequest.SHOULD_ADD_TO_SITE -> ChannelStatus.SHOULD_ADD_TO_SITE
            ContentPartnerStatusRequest.SHOULD_PROMOTE -> ChannelStatus.SHOULD_PROMOTE
            ContentPartnerStatusRequest.PROMOTED -> ChannelStatus.PROMOTED
        }
}

package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.channel.ChannelStatus
import com.boclips.videos.api.request.channel.ChannelStatusRequest

object ContentPartnerMarketingStatusConverter {
    fun convert(statusRequest: ChannelStatusRequest) =
        when (statusRequest) {
            ChannelStatusRequest.NEEDS_INTRODUCTION -> ChannelStatus.NEEDS_INTRODUCTION
            ChannelStatusRequest.HAVE_REACHED_OUT -> ChannelStatus.HAVE_REACHED_OUT
            ChannelStatusRequest.NEEDS_CONTENT -> ChannelStatus.NEEDS_CONTENT
            ChannelStatusRequest.WAITING_FOR_INGEST -> ChannelStatus.WAITING_FOR_INGEST
            ChannelStatusRequest.SHOULD_ADD_TO_SITE -> ChannelStatus.SHOULD_ADD_TO_SITE
            ChannelStatusRequest.SHOULD_PROMOTE -> ChannelStatus.SHOULD_PROMOTE
            ChannelStatusRequest.PROMOTED -> ChannelStatus.PROMOTED
        }
}

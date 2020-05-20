package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.channel.MarketingInformation
import com.boclips.videos.api.request.channel.ChannelRequest

object ContentPartnerMarketingInformationConverter {
    fun convert(channelRequest: ChannelRequest): MarketingInformation =
        MarketingInformation(
            oneLineDescription = channelRequest.oneLineDescription,
            status = channelRequest.marketingInformation?.status?.let(ContentPartnerMarketingStatusConverter::convert),
            logos = channelRequest.marketingInformation?.logos?.map(UrlConverter::convert),
            showreel = channelRequest.marketingInformation?.showreel?.map(UrlConverter::convert)?.orNull(),
            sampleVideos = channelRequest.marketingInformation?.sampleVideos?.map(UrlConverter::convert)
        )
}

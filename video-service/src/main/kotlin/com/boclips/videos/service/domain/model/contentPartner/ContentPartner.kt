package com.boclips.videos.service.domain.model.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.eventbus.domain.video.ContentPartner as EventContentPartner

data class ContentPartner(
    val contentPartnerId: ContentPartnerId,
    val name: String,
    val ageRange: AgeRange,
    val credit: Credit,
    val distributionMethods: Set<DistributionMethod>
) {
    fun isStreamable(): Boolean {
        return distributionMethods.contains(DistributionMethod.STREAM)
    }

    fun isDownloadable(): Boolean {
        return distributionMethods.contains(DistributionMethod.DOWNLOAD)
    }

    fun toEvent(): EventContentPartner {
        return EventContentPartner.of(name)
    }
}
